(ns store-service.routers.users-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.route :as route]
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

(def ^:private uuid-pattern
  #"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
(s/def ::uuid (s/and string? #(re-matches uuid-pattern %)))

(defroutes routes
  (context "/api/v1/store/:user-uid" {{user-uid :user-uid} :params
                                      {{claims-uid :user-uid} :claims} :params}
    (GET "/orders" []
      (validate-and-handle #(service/find-user-orders! (as-uuid claims-uid) (as-uuid %))
                           [::uuid user-uid]))
    (POST "/purchase" {:keys [body]}
      (validate-and-handle #(service/make-purchase! (as-uuid claims-uid) (as-uuid %) %2)
                           [::uuid user-uid]
                           [::purchase-item body]))
    (context "/:order-uid" [order-uid :<< as-uuid]
      (GET "/" []
        (validate-and-handle #(service/find-user-order! (as-uuid claims-uid) (as-uuid %) %2)
                             [::uuid user-uid]
                             [uuid? order-uid]))
      (POST "/warranty" {:keys [body]}
        (validate-and-handle #(service/warranty-request! (as-uuid claims-uid) (as-uuid %) %2 %3)
                             [::uuid user-uid]
                             [uuid? order-uid]
                             [::request-warranty body]))
      (DELETE "/refund" []
        (validate-and-handle  #(service/refund-purchase! (as-uuid claims-uid) (as-uuid %) %2)
                              [::uuid user-uid]
                              [uuid? order-uid]))))
  (route/not-found {:status 404}))

(def router (handler/api routes))