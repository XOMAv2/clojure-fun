(ns warehouse-service.routers.warehouse-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.handler :as handler]
            [warehouse-service.services.warehouse-service :as warehouse]
            [warehouse-service.services.warranty-service :as warranty]
            [warehouse-service.helpers.subroutines :refer [uuid]]))

(defroutes routes
  (context "/api/v1/warehouse" []
    (POST "/" request (warehouse/take-item! (-> request
                                                (:body)
                                                (#(assoc % :orderUid (uuid (:orderUid %)))))))
    (context "/:item-uid" [item-uid]
      (GET "/" [] (warehouse/get-item-info (uuid item-uid)))
      (DELETE "/" [] (warehouse/return-item! (uuid item-uid)))
      (POST "/warranty" request (warranty/warranty-request! (uuid item-uid)
                                                            (-> request
                                                                (:body)
                                                                (:reason))))))
  (fn [_] {:status 404}))

(def router (handler/api routes))