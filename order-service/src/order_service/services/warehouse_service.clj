(ns order-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [common-functions.uuid :refer [json-write-uuid]]))

(def warehouse-url (let [config (load-env)
                         env-type (:env-type config)
                         env (env-type (:env config))]
                     (:warehouse-url env)))

(defn take-item!
  [order-uid model size]
  (let [request {:orderUid order-uid
                 :model model
                 :size size}
        path (str warehouse-url "api/v1/warehouse")
        response (client/post path
                              {:body (json/write-str request
                                                     :value-fn json-write-uuid)
                               :headers {"Content-Type" "application/json"}})]
    response))

(defn return-item!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (client/delete path)]
    response))

(defn use-warranty-item!
  [item-uid request-body]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/warranty")
        response (client/post path {:body (json/write-str request-body)
                                    :headers {"Content-Type" "application/json"}})]
    response))