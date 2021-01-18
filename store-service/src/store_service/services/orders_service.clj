(ns store-service.services.orders-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [common-functions.uuid :refer [json-write-uuid]]))

(def orders-url (let [config (load-env)
                      env-type (:env-type config)
                      env (env-type (:env config))]
                     (:orders-url env)))

(def cb-get-order-info!
  (def-cb-service-call
    (fn [path]
      (client/get path))))

(defn get-order-info!
  [user-uid order-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid "/" order-uid)
        response (apply-cb-service-call cb-get-order-info! path)]
    response))

(def cb-get-order-info-by-user-uid!
  (def-cb-service-call
    (fn [path]
      (client/get path))))

(defn get-order-info-by-user-uid!
  [user-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (apply-cb-service-call cb-get-order-info-by-user-uid! path)]
    response))

(def cb-make-purchase!
  (def-cb-service-call
    (fn [path request]
      (client/post path
                   {:body (json/write-str request
                                          :value-fn json-write-uuid)
                    :headers {"Content-Type" "application/json"}}))))

(defn make-purchase!
  [user-uid request]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (apply-cb-service-call cb-make-purchase! path request)]
    response))

(def cb-refund-purchase!
  (def-cb-service-call
    (fn [path]
      (client/delete path))))

(defn refund-purchase!
  [order-uid]
  (let [path (str orders-url "api/v1/orders/" order-uid)
        response (apply-cb-service-call cb-refund-purchase! path)]
    response))

(def cb-warranty-request!
  (def-cb-service-call
    (fn [path request]
      (client/post path
                   {:body (json/write-str request
                                          :value-fn json-write-uuid)
                    :headers {"Content-Type" "application/json"}}))))

(defn warranty-request!
  [order-uid request]
  (let [path (str orders-url "api/v1/orders/" order-uid "/warranty")
        response (apply-cb-service-call cb-warranty-request! path request)]
    response))