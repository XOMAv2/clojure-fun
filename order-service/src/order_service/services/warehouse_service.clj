(ns order-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [common-functions.uuid :refer [json-write-uuid]]))

(def warehouse-url (let [config (load-env)
                         env-type (:env-type config)
                         env (env-type (:env config))]
                     (:warehouse-url env)))

(def cb-take-item!
  (def-cb-service-call
    (fn [path request]
      (client/post path
                   {:body (json/write-str request
                                          :value-fn json-write-uuid)
                    :headers {"Content-Type" "application/json"}}))))

(defn take-item!
  [order-uid model size]
  (let [request {:orderUid order-uid
                 :model model
                 :size size}
        path (str warehouse-url "api/v1/warehouse")
        response (apply-cb-service-call cb-take-item! path request)]
    response))

(def cb-rollback-take-item!
  (def-cb-service-call
    (fn [path request]
      (client/delete path
                     {:body (json/write-str request
                                            :value-fn json-write-uuid)
                      :headers {"Content-Type" "application/json"}}))))

(defn rollback-take-item!
  [order-item-uid]
  (let [request {:orderItemUid order-item-uid}
        path (str warehouse-url "api/v1/warehouse/rollback")
        response (apply-cb-service-call cb-rollback-take-item! path request)]
    response))

(def cb-return-item!
  (def-cb-service-call
    (fn [path]
      (client/delete path))))

(defn return-item!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (apply-cb-service-call cb-return-item! path)]
    response))

(def cb-rollback-return-item!
  (def-cb-service-call
    (fn [path]
      (client/post path))))

(defn rollback-return-item!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/rollback")
        response (apply-cb-service-call cb-rollback-return-item! path)]
    response))

(def cb-use-warranty-item!
  (def-cb-service-call
    (fn [path request-body]
      (client/post path {:body (json/write-str request-body)
                         :headers {"Content-Type" "application/json"}}))))

(defn use-warranty-item!
  [item-uid request-body]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/warranty")
        response (apply-cb-service-call cb-use-warranty-item! path request-body)]
    response))