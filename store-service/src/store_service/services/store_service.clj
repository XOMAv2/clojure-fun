(ns store-service.services.store-service
  (:require [store-service.services.orders-service :as orders]
            [store-service.services.users-service :as users]
            [clojure.data.json :as json]
            [config.core :refer [load-env]]
            [store-service.services.warehouse-service :as warehouse]
            [store-service.services.warranty-service :as warranty]
            [common-functions.helpers :refer [create-response]])
  (:use [slingshot.slingshot :only [try+]]))

(def store-url (let [config (load-env)
                     env-type (:env-type config)
                     env (env-type (:env config))]
                 (:store-url env)))

(defn find-user-orders!
  [user-uid]
  (try+
   (if (users/user-exists? user-uid)
     (let [user-orders (-> user-uid
                           (orders/get-order-info-by-user-uid!)
                           (:body)
                           (json/read-str :key-fn keyword))
           orders (map (fn [order-info]
                         (let [order-uid (:orderUid order-info)
                               item-uid (:itemUid order-info)
                               order {:orderUid order-uid
                                      :date (:orderDate order-info)}
                               warehouse-response (warehouse/get-item-info! item-uid)
                               warehouse-body (json/read-str (:body warehouse-response)
                                                             :key-fn keyword)
                               order (if (empty? warehouse-body)
                                       order
                                       (-> order
                                           (assoc :model (:model warehouse-body))
                                           (assoc :size (:size warehouse-body))))
                               warranty-response (warranty/get-item-warranty-info! item-uid)
                               warranty-body (json/read-str (:body warranty-response)
                                                            :key-fn keyword)
                               order (if (empty? warranty-body)
                                       order
                                       (-> order
                                           (assoc :warrantyDate (:warrantyDate warranty-body))
                                           (assoc :warrantyStatus (:warrantyStatus warranty-body))))]
                           order))
                       user-orders)]
       (create-response 200 orders))
     (create-response 404 {:message "User not found"}))
   (catch [:status 404] {:keys [status body headers]}
     {:status 404 :body body :headers headers})
   (catch [:status 500] {:keys [body headers]}
     {:status 422 :body body :headers headers})
   (catch Exception e
     (create-response 500 {:message (ex-message e)}))))

(defn find-user-order!
  [user-uid order-uid]
  (try+
   (if (users/user-exists? user-uid)
     (let [order-response (orders/get-order-info! user-uid order-uid)
           order-body (json/read-str (:body order-response)
                                     :key-fn keyword)
           response {:orderUid order-uid
                     :date (:orderDate order-body)}
           item-uid (:itemUid order-body)
           warehouse-response (warehouse/get-item-info! item-uid)
           warehouse-body (json/read-str (:body warehouse-response)
                                         :key-fn keyword)
           response (if (empty? warehouse-body)
                      response
                      (-> response
                          (assoc :model (:model warehouse-body))
                          (assoc :size (:size warehouse-body))))
           warranty-response (warranty/get-item-warranty-info! item-uid)
           warranty-body (json/read-str (:body warranty-response)
                                        :key-fn keyword)
           response (if (empty? warranty-body)
                      response
                      (-> response
                          (assoc :warrantyDate (:warrantyDate warranty-body))
                          (assoc :warrantyStatus (:warrantyStatus warranty-body))))]
       (create-response 200 response))
     (create-response 404 {:message "User not found"}))
   (catch [:status 404] {:keys [status body headers]}
     {:status 404 :body body :headers headers})
   (catch [:status 500] {:keys [body headers]}
     {:status 422 :body body :headers headers})
   (catch Exception e
     (create-response 500 {:message (ex-message e)}))))

(defn make-purchase!
  [user-uid request]
  (try+
   (if (users/user-exists? user-uid)
     (let [response (orders/make-purchase! user-uid request)
           body (json/read-str (:body response)
                               :key-fn keyword)]
       {:status 201
        :headers {"location" (str store-url
                                  "api/v1/store/"
                                  user-uid
                                  "/"
                                  (:orderUid body))}})
     (create-response 404 {:message "User not found"}))
   (catch Exception e
     (create-response 500 {:message (ex-message e)}))))

(defn refund-purchase!
  [user-uid order-uid]
  (try+
   (if (users/user-exists? user-uid)
     (do
       (orders/refund-purchase! order-uid)
       {:status 204})
     (create-response 404 {:message "User not found"}))
   (catch Exception e
     (create-response 500 {:message (ex-message e)}))))

(defn warranty-request!
  [user-uid order-uid request]
  (try+
   (if (users/user-exists? user-uid)
     (let [response (orders/warranty-request! order-uid request)
           body (json/read-str (:body response)
                               :key-fn keyword)]
       (create-response 200
                        {:orderUid order-uid
                         :warrantyDate (:warrantyDate body)
                         :decision (:decision body)}))
     (create-response 404 {:message "User not found"}))
   (catch Exception e
     (create-response 500 {:message (ex-message e)}))))