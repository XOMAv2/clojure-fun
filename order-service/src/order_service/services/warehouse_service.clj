(ns order-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [common-functions.auth-service :refer [auth-request]]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [common-functions.uuid :refer [json-write-uuid]])
  (:use [slingshot.slingshot :only [try+]]))

(def warehouse-url (let [config (load-env)
                         env-type (:env-type config)
                         env (env-type (:env config))]
                     (:warehouse-url env)))

(def name "order-service")
(def password "order-service")
(def auth-path (str warehouse-url "api/v1/warehouse/auth"))
(def access-token (atom (auth-request name password auth-path)))

(def cb-take-item!
  (def-cb-service-call
    (fn [path request]
      (try+
       (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                          :headers {"Content-Type" "application/json"
                                    "Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                            :headers {"Content-Type" "application/json"
                                      "Authorization" (str "Bearer " @access-token)}}))))))

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
      (try+
       (client/delete path {:body (json/write-str request :value-fn json-write-uuid)
                            :headers {"Content-Type" "application/json"
                                      "Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/delete path {:body (json/write-str request :value-fn json-write-uuid)
                              :headers {"Content-Type" "application/json"
                                        "Authorization" (str "Bearer " @access-token)}}))))))

(defn rollback-take-item!
  [order-item-uid]
  (let [request {:orderItemUid order-item-uid}
        path (str warehouse-url "api/v1/warehouse/rollback")
        response (apply-cb-service-call cb-rollback-take-item! path request)]
    response))

(def cb-return-item!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/delete path {"Authorization" (str "Bearer " @access-token)})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/delete path {"Authorization" (str "Bearer " @access-token)}))))))

(defn return-item!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (apply-cb-service-call cb-return-item! path)]
    response))

(def cb-rollback-return-item!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/post path {"Authorization" (str "Bearer " @access-token)})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/post path {"Authorization" (str "Bearer " @access-token)}))))))

(defn rollback-return-item!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/rollback")
        response (apply-cb-service-call cb-rollback-return-item! path)]
    response))

(def cb-use-warranty-item!
  (def-cb-service-call
    (fn [path request-body]
      (try+
       (client/post path {:body (json/write-str request-body)
                          :headers {"Content-Type" "application/json"
                                    "Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/post path {:body (json/write-str request-body)
                            :headers {"Content-Type" "application/json"
                                      "Authorization" (str "Bearer " @access-token)}}))))))

(defn use-warranty-item!
  [item-uid request-body]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/warranty")
        response (apply-cb-service-call cb-use-warranty-item! path request-body)]
    response))