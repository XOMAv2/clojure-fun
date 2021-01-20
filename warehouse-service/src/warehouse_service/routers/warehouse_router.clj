(ns warehouse-service.routers.warehouse-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [validate-and-handle]]
            [warehouse-service.services.warehouse-service :as warehouse]
            [warehouse-service.services.warranty-service :as warranty]))

(s/def ::reason string?)

(s/def ::request-item-warranty (s/keys :req-un [::reason]))

(def ^:private uuid-pattern
  #"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
(s/def ::orderUid (s/and string? #(re-matches uuid-pattern %)))
(s/def ::model string?)
(s/def ::size string?)

(s/def ::take-item-from-warehouse (s/keys :req-un [::orderUid
                                                   ::model
                                                   ::size]))

(s/def ::orderItemUid (s/and string? #(re-matches uuid-pattern %)))

(defroutes routes
  (context "/api/v1/warehouse" []
    (POST "/" {:keys [body]}
      (validate-and-handle #(warehouse/take-item! (update % :orderUid as-uuid))
                           [::take-item-from-warehouse body]))
    (DELETE "/rollback" {{order-item-uid :orderItemUid } :body}
      (validate-and-handle #(warehouse/rollback-take-item! (as-uuid %))
                           [::orderItemUid order-item-uid]))
    (context "/:item-uid" [item-uid :<< as-uuid]
      (GET "/" [] (validate-and-handle warehouse/get-item-info!
                                       [uuid? item-uid]))
      (DELETE "/" [] (validate-and-handle warehouse/return-item!
                                          [uuid? item-uid]))
      (POST "/rollback" [] (validate-and-handle warehouse/rollback-return-item!
                                                [uuid? item-uid]))
      (POST "/warranty" {:keys [body]}
        (validate-and-handle #(warranty/warranty-request! % (:reason %2))
                             [uuid? item-uid]
                             [::request-item-warranty body]))))
  (route/not-found {:status 404}))

(def router (handler/api routes))