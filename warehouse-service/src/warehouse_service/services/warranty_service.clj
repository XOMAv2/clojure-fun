(ns warehouse-service.services.warranty-service
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [config.core :refer [load-env]]
            [common-functions.uuid :refer [json-write-uuid]]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call
                                              create-response]]
            [warehouse-service.services.warehouse-service :as warehouse])
  (:use [slingshot.slingshot :only [try+]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(def ^{:const true}
  default-exchange-name "")

(def cb-warranty-request!
  (def-cb-service-call
    (fn [path request]
      (client/post path {:body (json/write-str request)
                         :headers {"Content-Type" "application/json"}}))))

(defn warranty-request!
  [order-item-uid reason]
  (try+ (let [available-count (warehouse/check-item-available-count order-item-uid)
              request {:reason reason
                       :availableCount available-count}
              path (str warranty-url "api/v1/warranty/" order-item-uid "/warranty")
              response (try+
                        (apply-cb-service-call cb-warranty-request! path request)
                        (catch [:status 503] _
                          (let [config (load-env)
                                env-type (:env-type config)
                                amqp-url (-> config
                                             (:env)
                                             (env-type)
                                             (:amqp-url))
                                conn (rmq/connect {:uri amqp-url})
                                channel (lch/open conn)
                                qname "warranty-queue"]
                            (lq/declare channel qname {:exclusive false
                                                       :auto-delete true})
                            (lb/publish channel
                                        default-exchange-name
                                        qname
                                        (json/write-str {:item-uid order-item-uid
                                                         :body request}
                                                        :value-fn json-write-uuid)
                                        {:content-type "application/json"})
                            (rmq/close channel)
                            (rmq/close conn)
                            nil)))
              map-body (when (not (empty? (:body response)))
                         (json/read-str (:body response) :key-fn keyword))]
          (create-response 200 map-body))
        (catch [:status 404] {:as response}
          response)
        (catch #(#{500 503} (:status %)) {:as response}
          (assoc response :status 422))
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))