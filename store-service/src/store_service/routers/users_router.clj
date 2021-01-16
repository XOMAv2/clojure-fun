(ns store-service.routers.users-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [validate-and-handle]]
            [store-service.services.store-service :as service]))

(s/def ::model string?)
(s/def ::size string?)

(s/def ::purchase-item (s/keys :req-un [::model
                                        ::size]))

(s/def ::reason string?)

(s/def ::request-warranty (s/keys :req-un [::reason]))

(defroutes routes
  (context "/api/v1/store/:user-uid" [user-uid :<< as-uuid]
    (GET "/orders" [] (validate-and-handle service/find-user-orders!
                                           [uuid? user-uid]))
    (POST "/purchase" {:keys [body]} (validate-and-handle service/make-purchase!
                                                          [uuid? user-uid]
                                                          [::purchase-item body]))
    (context "/:order-uid" [order-uid :<< as-uuid]
      (GET "/" [] (validate-and-handle service/find-user-order!
                                       [uuid? user-uid]
                                       [uuid? order-uid]))
      (POST "/warranty" {:keys [body]} (validate-and-handle service/warranty-request!
                                                            [uuid? user-uid]
                                                            [uuid? order-uid]
                                                            [::request-warranty body]))
      (DELETE "/refund" [] (validate-and-handle service/refund-purchase!
                                                [uuid? user-uid]
                                                [uuid? order-uid]))))
  (fn [_] {:status 404}))

(def router (handler/api routes))