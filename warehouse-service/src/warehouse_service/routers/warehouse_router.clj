(ns warehouse-service.routers.warehouse-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [warehouse-service.services.warehouse-service :as warehouse]
            [warehouse-service.services.warranty-service :as warranty]))

(defroutes routes
  (context "/api/v1/warehouse" []
    (POST "/" {:keys [body]} (warehouse/take-item! (update body :orderUid as-uuid)))
    (context "/:item-uid" [item-uid :<< as-uuid]
      (GET "/" [] (warehouse/get-item-info! item-uid))
      (DELETE "/" [] (warehouse/return-item! item-uid))
      (POST "/warranty" {:keys [body]} (warranty/warranty-request! item-uid (:reason body)))))
  (fn [_] {:status 404}))

(def router (handler/api routes))