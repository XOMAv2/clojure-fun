(ns store-service.routers.users-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.handler :as handler]
            [store-service.services.store-service :as service]
            [store-service.helpers.subroutines :refer [uuid]]))

(defroutes routes
  (context "/api/v1/store/:user-uid" [user-uid]
    (GET "/orders" [] (service/find-user-orders! (uuid user-uid)))
    (POST "/purchase" request (service/make-purchase! (uuid user-uid) (:body request)))
    (context "/:order-uid" [order-uid]
      (GET "/" [] (service/find-user-order! (uuid user-uid) (uuid order-uid)))
      (POST "/warranty" request (service/warranty-request! (uuid user-uid)
                                                           (uuid order-uid)
                                                           (:body request)))
      (DELETE "/refund" [] (service/refund-purchase! (uuid user-uid) (uuid order-uid)))))
  (fn [_] {:status 404}))

(def router (handler/api routes))