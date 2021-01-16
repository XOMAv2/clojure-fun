(ns warranty-service.routers.warranty-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [warranty-service.services.warranty-service :as service]))

(defroutes routes
  (context "/api/v1/warranty/:item-uid" [item-uid :<< as-uuid]
    (GET "/" [] (service/get-warranty! item-uid))
    (POST "/" [] (service/start-warranty! item-uid))
    (DELETE "/" [] (service/close-warranty! item-uid))
    (POST "/warranty" {:keys [body]} (service/warranty-decision! item-uid body)))
  (fn [_] {:status 404}))

(def router (handler/api routes))