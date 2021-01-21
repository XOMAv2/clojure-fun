(ns warehouse-service.routers.warehouse-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context routes wrap-routes]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [compojure.route :refer [not-found]]
            [buddy.core.keys :as keys]
            [common-functions.auth-service :as auth-service]
            [common-functions.middlewares :refer [jwt-authorization]]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [valid?-handle]]
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

(defroutes public-routes
  (POST "/api/v1/warehouse/auth" {{auth "authorization"} :headers}
    (auth-service/auth auth)))

(defroutes private-routes
  (context "/api/v1/warehouse" []
    (POST "/" {:keys [body]}
      (valid?-handle #(warehouse/take-item! (update % :orderUid as-uuid))
                     [::take-item-from-warehouse body]))
    (DELETE "/rollback" {{order-item-uid :orderItemUid} :body}
      (valid?-handle #(warehouse/rollback-take-item! (as-uuid %))
                     [::orderItemUid order-item-uid]))
    (context "/:item-uid" [item-uid :<< as-uuid]
      (GET "/" [] (valid?-handle warehouse/get-item-info!
                                 [uuid? item-uid]))
      (DELETE "/" [] (valid?-handle warehouse/return-item!
                                    [uuid? item-uid]))
      (POST "/rollback" [] (valid?-handle warehouse/rollback-return-item!
                                          [uuid? item-uid]))
      (POST "/warranty" {:keys [body]}
        (valid?-handle #(warranty/warranty-request! % (:reason %2))
                       [uuid? item-uid]
                       [::request-item-warranty body])))))

(def router (handler/api (routes public-routes
                                 private-routes
                                 (wrap-routes private-routes
                                              jwt-authorization
                                              (keys/public-key "jwtRS256.key.pub"))
                                 (not-found {:status 404}))))