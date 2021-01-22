(ns order-service.services.order-managment-service
  (:require [order-service.services.orders-service :as orders]
            [order-service.services.warehouse-service :as warehouse]
            [order-service.services.warranty-service :as warranty]
            [common-functions.uuid :refer [uuid random-uuid]]
            [common-functions.helpers :refer [create-response]]
            [clojure.data.json :as json])
  (:use [slingshot.slingshot :only [try+ throw+]]))

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
                                 (uuid))]
          (try+ (warranty/start-warranty! order-item-uid)
                (try+ (orders/create-order! order-uid user-uid order-item-uid)
                      (create-response 200 {:orderUid order-uid})
                      (catch #(#{500 503} (:status %)) {:as response}
                        (warehouse/rollback-take-item! order-item-uid)
                        (warranty/stop-warranty! order-item-uid)
                        (throw+ response)))
                (catch #(#{500 503} (:status %)) {:as response}
                  (warehouse/rollback-take-item! order-item-uid)
                  (throw+ response))))
        (catch [:status 404] {:as response}
          response)
        (catch #(#{500 503} (:status %)) {:as response}
          (assoc response :status 422))
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))

(defn refund-order!
  [order-uid]
  (try+ (let [order (orders/get-order-by-order-uid! order-uid)
              item-uid (:item_uid order)
              _ (warehouse/return-item! item-uid)]
          (try+ (warranty/stop-warranty! item-uid)
                (try+ (orders/cancel-order! order-uid)
                      {:status 204}
                      (catch #(#{500 503} (:status %)) {:as response}
                        (warehouse/rollback-return-item! item-uid)
                        (warranty/start-warranty! item-uid)
                        (throw+ response)))
                (catch #(#{500 503} (:status %)) {:as response}
                  (warehouse/rollback-return-item! item-uid)
                  (throw+ response))))
        (catch #(#{500 503} (:status %)) {:as response}
          (assoc response :status 422))
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))

(defn use-warranty!
  [order-uid request-body]
  (try+ (let [order (orders/get-order-by-order-uid! order-uid)
              item-uid (:item_uid order)
              warehouse-response (warehouse/use-warranty-item! item-uid request-body)]
          warehouse-response)
        (catch #(#{500 503} (:status %)) {:as response}
          (assoc response :status 422))
        (catch [:status 404] {:as response}
          response)
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))