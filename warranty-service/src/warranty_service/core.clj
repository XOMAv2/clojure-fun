(ns warranty-service.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [warranty-service.routers.warranty-router
             :refer [router validate-and-make-warranty-decision!]
             :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
            [warranty-service.entities.warranty :refer [*db-spec* warranty-table-spec]]
            [clojure.data.json :as json]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [common-functions.db :refer [create-table-if-not-exists!]])
  (:gen-class))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      (remove-utf-8-from-header)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})))

(def ^{:const true}
  default-exchange-name "")

(defn message-handler
  [ch {:as meta} ^bytes payload]
  (try (let [unpacked (json/read-str payload :key-fn keyword)
             item-uid (:item-uid unpacked)
             body (:body unpacked)]
         (validate-and-make-warranty-decision! item-uid body))
       (catch Exception _)))

(defn -main
  [& args]
  (create-table-if-not-exists! *db-spec* warranty-table-spec)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false})
  (let [config (load-env)
        env-type (:env-type config)
        amqp-url (-> config
                     (:env)
                     (env-type)
                     (:amqp-url))
        conn (rmq/connect {:uri amqp-url})
        channel (lch/open conn)
        qname "warranty-queue"]
    (lq/declare channel qname {:exclusive false :auto-delete true})
    (lc/subscribe channel qname message-handler {:auto-ack true})))