(ns warehouse-service.routers.warehouse-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [warehouse-service.services.warehouse-service :as warehouse]
            [warehouse-service.services.warranty-service :as warranty]))

(defroutes routes
  (context "/api/v1/warehouse" []
    (POST "/" request (warehouse/take-item! (-> request
                                                (:body)
                                                (#(assoc % :orderUid (as-uuid (:orderUid %)))))))
    (context "/:item-uid" [item-uid :<< as-uuid]
      (GET "/" [] (warehouse/get-item-info! item-uid))
      (DELETE "/" [] (warehouse/return-item! item-uid))
      (POST "/warranty" request (warranty/warranty-request! item-uid
                                                            (-> request
                                                                (:body)
                                                                (:reason))))))
  (fn [_] {:status 404}))

(def router (handler/api routes))