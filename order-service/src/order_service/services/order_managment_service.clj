(ns order-service.services.order-managment-service
  (:require [order-service.services.orders-service :as orders]
            [order-service.services.warehouse-service :as warehouse]
            [order-service.services.warranty-service :as warranty]
            [order-service.helpers.subroutines :refer [random-uuid
                                                       create-response
                                                       uuid]]
            [clojure.data.json :as json])
  (:use [slingshot.slingshot :only [try+]]))

(defn make-order!
  [user-uid request-body]
  (try+ (let [model (:model request-body)
              size (:size request-body)
              order-uid (random-uuid)
              warehouse-response (-> order-uid
                                     (warehouse/take-item! model size)
                                     (:body)
                                     (json/read-str :key-fn keyword))
              order-item-uid (-> warehouse-response
                                 (:orderItemUid)
                                 (uuid))
              _ (warranty/start-warranty! order-item-uid)
              _ (orders/create-order! order-uid user-uid order-item-uid)]
          (create-response 200 {:orderUid order-uid}))
        (catch [:status 404] {:keys [status body headers]}
          {:status 404 :body body :headers headers})
        (catch [:status 500] {:keys [body headers]}
          {:status 422 :body body :headers headers})
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))

(defn refund-order!
  [order-uid]
  (try+ (let [order (orders/get-order-by-order-uid! order-uid)
              item-uid (:item_uid order)
              _ (warehouse/return-item! item-uid)
              _ (warranty/stop-warranty! item-uid)
              _ (orders/cancel-order! order-uid)]
          {:status 204})
        (catch [:status 500] {:keys [body headers]}
          {:status 422 :body body :headers headers})
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))

(defn use-warranty!
  [order-uid request-body]
  (try+ (let [order (orders/get-order-by-order-uid! order-uid)
              item-uid (:item_uid order)
              warehouse-response (warehouse/use-warranty-item! item-uid request-body)]
          warehouse-response)
        (catch [:status 500] {:keys [body headers]}
          {:status 422 :body body :headers headers})
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))