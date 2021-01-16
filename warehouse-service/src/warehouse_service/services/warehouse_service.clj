(ns warehouse-service.services.warehouse-service
  (:require [warehouse-service.repositories.items-repository :as i-rep]
            [warehouse-service.repositories.order-item-repository :as oi-rep]
            [warehouse-service.helpers.subroutines :refer [create-response
                                                           random-uuid]])
  (:use [clojure.set :only [rename-keys]]))

(defn get-item-info!
  [order-item-uid]
  (try (let [item (i-rep/get-item-by-order-item-uid! order-item-uid)]
         (if item
           (create-response 200
                            {:model (:model item)
                             :size (:size item)})
           (create-response 404
                            {:message "The item with the specified order-item-uid was not found."})))
       (catch Exception e (create-response 500  {:message (ex-message e)}))))

(defn get-order-item!
  [order-item-uid]
  (try (let [item (i-rep/get-item-by-order-item-uid! order-item-uid)]
         (if item
           (create-response 200
                            (rename-keys item {:available_count :availableCount}))
           (create-response 404
                            {:message "The item with the specified order-item-uid was not found."})))
       (catch Exception e (create-response 500  {:message (ex-message e)}))))

(defn take-item!
  [request]
  (try (let [model (:model request)
             size (:size request)
             order-uid (:orderUid request)
             item (i-rep/get-item-by-model-and-size! model size)]
         (if (= item nil)
           (create-response 404 {:message "Requested item not found"})
           (if (< (:available_count item) 1)
             (throw (Exception. "Item is finished on warehouse"))
             (let [order-item {:item_id (:id item)
                               :order_uid order-uid
                               :order_item_uid (random-uuid)
                               :canceled false}]
               (oi-rep/add-order-item! order-item)
               (i-rep/take-one-item! (:id item))
               (create-response 200
                                {:orderItemUid (:order_item_uid order-item)
                                 :orderUid (:order_uid order-item)
                                 :size (:size item)
                                 :model (:model item)})))))
       (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn return-item!
  [order-item-uid]
  (try (i-rep/return-one-item! order-item-uid)
       (oi-rep/cancel-order-item! order-item-uid)
       {:status 204}
       (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn check-item-available-count
  [order-item-uid]
  (try (let [item (i-rep/get-item-by-order-item-uid! order-item-uid)]
         (:available_count item))
       (catch Exception e (create-response 500 {:message (ex-message e)}))))