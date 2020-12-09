(ns order-service.routers.orders-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.handler :as handler]
            [order-service.services.orders-service :as orders]
            [order-service.services.order-managment-service :as order-managment]
            [order-service.helpers.subroutines :refer [uuid]]))

(defroutes routes
  (context "/api/v1/orders/:uid" [uid]
    (GET "/" [] (orders/get-user-orders! (uuid uid)))
    (POST "/" request (order-managment/make-order! (uuid uid) (:body request)))
    (DELETE "/" [] (order-managment/refund-order! (uuid uid)))
    (POST "/warranty" request (order-managment/use-warranty! (uuid uid) (:body request)))
    (GET "/:order-uid" [order-uid] (orders/get-user-order! (uuid uid) (uuid order-uid))))
  (fn [_] {:status 404}))

(def router (handler/api routes))