(ns store-service.routers.users-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [store-service.services.store-service :as service]))

(defroutes routes
  (context "/api/v1/store/:user-uid" [user-uid :<< as-uuid]
    (GET "/orders" [] (service/find-user-orders! user-uid))
    (POST "/purchase" {:keys [body]} (service/make-purchase! user-uid body))
    (context "/:order-uid" [order-uid :<< as-uuid]
      (GET "/" [] (service/find-user-order! user-uid order-uid))
      (POST "/warranty" {:keys [body]} (service/warranty-request! user-uid
                                                           order-uid
                                                           body))
      (DELETE "/refund" [] (service/refund-purchase! user-uid order-uid))))
  (fn [_] {:status 404}))

(def router (handler/api routes))