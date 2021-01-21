(ns order-service.routers.orders-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context routes wrap-routes]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [compojure.route :refer [not-found]]
            [common-functions.middlewares :refer [jwt-authorization]]
            [common-functions.auth-service :as auth-service]
            [buddy.core.keys :as keys]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [valid?-handle]]
            [order-service.services.orders-service :as orders]
            [order-service.services.order-managment-service :as order-managment]))

(s/def ::reason string?)

(s/def ::warranty-request (s/keys :req-un [::reason]))

(s/def ::model string?)
(s/def ::size string?)

(s/def ::create-order (s/keys :req-un [::model
                                       ::size]))

(defroutes public-routes
  (POST "/api/v1/orders/auth" {{auth "authorization"} :headers}
    (auth-service/auth auth)))

(defroutes private-routes
  (context "/api/v1/orders/:uid" [uid :<< as-uuid]
    (GET "/" [] (valid?-handle orders/get-user-orders!
                               [uuid? uid]))
    (POST "/" {:keys [body]} (valid?-handle order-managment/make-order!
                                            [uuid? uid]
                                            [::create-order body]))
    (DELETE "/" [] (valid?-handle order-managment/refund-order!
                                  [uuid? uid]))
    (POST "/warranty" {:keys [body]} (valid?-handle order-managment/use-warranty!
                                                    [uuid? uid]
                                                    [::warranty-request body]))
    (GET "/:order-uid" [order-uid :<< as-uuid] (valid?-handle orders/get-user-order!
                                                              [uuid? uid]
                                                              [uuid? order-uid]))))

(def router (handler/api (routes public-routes
                                 (wrap-routes private-routes
                                              jwt-authorization
                                              (keys/public-key "jwtRS256.key.pub"))
                                 (not-found {:status 404}))))