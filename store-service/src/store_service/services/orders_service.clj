(ns store-service.services.orders-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [common-functions.base64 :refer [str->base64str]]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [common-functions.uuid :refer [json-write-uuid]])
  (:use [slingshot.slingshot :only [try+]]))

(def orders-url (let [config (load-env)
                      env-type (:env-type config)
                      env (env-type (:env config))]
                     (:orders-url env)))

(def name "store-service")
(def password "store-service")
(def auth-path (str orders-url "api/v1/orders/auth"))
(def access-token (atom (let [header (str->base64str (str name ":" password))
                              header (str "Basic " header)
                              response (client/post auth-path {:headers {"Authorization" header}})
                              claims (json/read-str (:body response) :key-fn keyword)]
                          (:accessToken claims))))

(def cb-get-order-info!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (let [header (str->base64str (str name ":" password))
               header (str "Basic " header)
               response (client/post auth-path {:headers {"Authorization" header}})
               claims (json/read-str (:body response) :key-fn keyword)]
           (swap! access-token (fn [x] (:accessToken claims)))
           (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})))))))

(defn get-order-info!
  [user-uid order-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid "/" order-uid)
        response (apply-cb-service-call cb-get-order-info! path)]
    response))

(def cb-get-order-info-by-user-uid!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (let [header (str->base64str (str name ":" password))
               header (str "Basic " header)
               response (client/post auth-path {:headers {"Authorization" header}})
               claims (json/read-str (:body response) :key-fn keyword)]
           (swap! access-token (fn [x] (:accessToken claims)))
           (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})))))))

(defn get-order-info-by-user-uid!
  [user-uid]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (apply-cb-service-call cb-get-order-info-by-user-uid! path)]
    response))

(def cb-make-purchase!
  (def-cb-service-call
    (fn [path request]
      (try+
       (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                          :headers {"Content-Type" "application/json"
                                    "Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (let [header (str->base64str (str name ":" password))
               header (str "Basic " header)
               response (client/post auth-path {:headers {"Authorization" header}})
               claims (json/read-str (:body response) :key-fn keyword)]
           (swap! access-token (fn [x] (:accessToken claims)))
           (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                              :headers {"Content-Type" "application/json"
                                        "Authorization" (str "Bearer " @access-token)}})))))))

(defn make-purchase!
  [user-uid request]
  (let [path (str orders-url "api/v1/orders/" user-uid)
        response (apply-cb-service-call cb-make-purchase! path request)]
    response))

(def cb-refund-purchase!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/delete path {:headers {"Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (let [header (str->base64str (str name ":" password))
               header (str "Basic " header)
               response (client/post auth-path {:headers {"Authorization" header}})
               claims (json/read-str (:body response) :key-fn keyword)]
           (swap! access-token (fn [x] (:accessToken claims)))
           (client/delete path {:headers {"Authorization" (str "Bearer " @access-token)}})))))))

(defn refund-purchase!
  [order-uid]
  (let [path (str orders-url "api/v1/orders/" order-uid)
        response (apply-cb-service-call cb-refund-purchase! path)]
    response))

(def cb-warranty-request!
  (def-cb-service-call
    (fn [path request]
      (try+
       (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                          :headers {"Content-Type" "application/json"
                                    "Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (let [header (str->base64str (str name ":" password))
               header (str "Basic " header)
               response (client/post auth-path {:headers {"Authorization" header}})
               claims (json/read-str (:body response) :key-fn keyword)]
           (swap! access-token (fn [x] (:accessToken claims)))
           (client/post path {:body (json/write-str request :value-fn json-write-uuid)
                              :headers {"Content-Type" "application/json"
                                        "Authorization" (str "Bearer " @access-token)}})))))))

(defn warranty-request!
  [order-uid request]
  (let [path (str orders-url "api/v1/orders/" order-uid "/warranty")
        response (apply-cb-service-call cb-warranty-request! path request)]
    response))