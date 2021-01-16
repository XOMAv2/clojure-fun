(ns order-service.routers.orders-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [validate-and-handle]]
            [order-service.services.orders-service :as orders]
            [order-service.services.order-managment-service :as order-managment]))

(s/def ::reason string?)

(s/def ::warranty-request (s/keys :req-un [::reason]))

(s/def ::model string?)
(s/def ::size string?)

(s/def ::create-order (s/keys :req-un [::model
                                       ::size]))

(defroutes routes
  (context "/api/v1/orders/:uid" [uid :<< as-uuid]
    (GET "/" [] (validate-and-handle orders/get-user-orders!
                                     [uuid? uid]))
    (POST "/" {:keys [body]} (validate-and-handle order-managment/make-order!
                                                  [uuid? uid]
                                                  [::create-order body]))
    (DELETE "/" [] (validate-and-handle order-managment/refund-order!
                                        [uuid? uid]))
    (POST "/warranty" {:keys [body]} (validate-and-handle order-managment/use-warranty!
                                                          [uuid? uid]
                                                          [::warranty-request body]))
    (GET "/:order-uid" [order-uid :<< as-uuid] (validate-and-handle orders/get-user-order!
                                                                    [uuid? uid]
                                                                    [uuid? order-uid])))
  (fn [_] {:status 404}))

(def router (handler/api routes))