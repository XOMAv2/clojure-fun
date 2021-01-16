(ns order-service.routers.orders-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [order-service.services.orders-service :as orders]
            [order-service.services.order-managment-service :as order-managment]))

(defroutes routes
  (context "/api/v1/orders/:uid" [uid :<< as-uuid]
    (GET "/" [] (orders/get-user-orders! uid))
    (POST "/" {:keys [body]} (order-managment/make-order! uid body))
    (DELETE "/" [] (order-managment/refund-order! uid))
    (POST "/warranty" {:keys [body]} (order-managment/use-warranty! uid body))
    (GET "/:order-uid" [order-uid :<< as-uuid] (orders/get-user-order! uid order-uid)))
  (fn [_] {:status 404}))

(def router (handler/api routes))