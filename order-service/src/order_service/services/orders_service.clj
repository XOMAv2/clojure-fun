(ns order-service.services.orders-service
  (:require [order-service.repositories.orders-repository :as rep]
            [common-functions.helpers :refer [create-response]]
            [java-time :as time])
  (:use [slingshot.slingshot :only [throw+]]))

(defn get-user-order!
  [user-uid order-uid]
  (try (let [order (rep/get-order-by-user-uid-and-order-uid! user-uid
                                                             order-uid)]
         (if order
           (create-response 200
                            {:orderUid (:order_uid order)
                             :itemUid (:item_uid order)
                             :status (:status order)
                             :orderDate (:order_date order)})
           (create-response 404
                            {:message "The order with the specified user-uid and order-uid was not found."})))
       (catch Exception e (create-response 500  {:message (ex-message e)}))))

(defn get-user-orders!
  [user-uid]
  (try (let [orders (rep/get-orders-by-user-uid! user-uid)]
         (create-response 200
                          (vec (map (fn [order] {:orderUid (:order_uid order)
                                                 :itemUid (:item_uid order)
                                                 :status (:status order)
                                                 :orderDate (:order_date order)})
                                    orders))))
       (catch Exception e (create-response 500  {:message (ex-message e)}))))

(defn get-order-by-order-uid!
  [order-uid]
  (let [order (rep/get-order-by-order-uid! order-uid)]
    (if (nil? order)
      (throw+ {:status 404 
               :body {:message "The order with the specified order-uid was not found."}})
      order)))
    
(defn create-order!
  [order-uid user-uid item-uid]
  (rep/add-order! {:user_uid user-uid
                   :order_uid order-uid
                   :order_date (time/sql-timestamp (time/local-date-time))
                   :status "PAID"
                   :item_uid item-uid}))

(defn cancel-order!
  [order-uid]
  (rep/delete-order! order-uid))

(defn order-exists?
  [order-uid]
  (-> order-uid
      (rep/get-order-by-order-uid!)
      (not= nil)))