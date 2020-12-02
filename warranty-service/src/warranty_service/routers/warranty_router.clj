(ns warranty-service.routers.warranty-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.handler :as handler]
            [warranty-service.services.warranty-service :as service]
            [warranty-service.helpers.subroutines :refer [uuid]]))

(def u (java.util.UUID/fromString "4fe5d828-6444-11e8-8222-720007e40350"))

(defroutes routes
  (context "/api/v1/warranty" []
    (context "/:item-uid" [item-uid]
      (GET "/" [] (service/get-warranty (uuid item-uid)))
      (POST "/" [] (service/start-warranty! (uuid item-uid)))
      (DELETE "/" [] (service/close-warranty! (uuid item-uid)))
      (POST "/warranty" request (service/warranty-decision! (uuid item-uid) (:body request)))))
  (fn [_] {:status 404}))

(def router (handler/api routes))