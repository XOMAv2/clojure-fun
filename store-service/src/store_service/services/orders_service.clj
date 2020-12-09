(ns store-service.services.orders-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [store-service.helpers.subroutines :refer [json-write-uuid]]))

(def orders-url (let [config (load-env)
                      env-type (:env-type config)
                      env (env-type (:env config))]
                     (:orders-url env)))

(defn get-order-info!
  [user-uid order-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid "/" order-uid)
        response (client/get path)]
    response))

(defn get-order-info-by-user-uid!
  [user-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (client/get path)]
    response))

(defn make-purchase!
  [user-uid request]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (client/post path
                              {:body (json/write-str request
                                                     :value-fn json-write-uuid)
                               :headers {"Content-Type" "application/json"}})]
    response))

(defn refund-purchase!
  [order-uid]
  (let [path (str orders-url "api/v1/orders/" order-uid)
        response (client/delete path)]
    response))

(defn warranty-request!
  [order-uid request]
  (let [path (str orders-url "api/v1/orders/" order-uid "/warranty")
        response (client/post path
                              {:body (json/write-str request
                                                     :value-fn json-write-uuid)
                               :headers {"Content-Type" "application/json"}})]
    response))