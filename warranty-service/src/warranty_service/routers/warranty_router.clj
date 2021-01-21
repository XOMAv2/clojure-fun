(ns warranty-service.routers.warranty-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [compojure.route :refer [not-found]]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [validate-and-handle]]
            [warranty-service.services.warranty-service :as service]))

(s/def ::reason string?)
(s/def ::availableCount int?)

(s/def ::request-warranty-decision (s/keys :req-un [::reason
                                                    ::availableCount]))

(defn validate-and-make-warranty-decision!
  [item-uid body]
  (validate-and-handle service/warranty-decision!
                       [uuid? item-uid]
                       [::request-warranty-decision body]))

(defroutes routes
  (context "/api/v1/warranty/:item-uid" [item-uid :<< as-uuid]
    (GET "/" [] (validate-and-handle service/get-warranty!
                                     [uuid? item-uid]))
    (POST "/" [] (validate-and-handle service/start-warranty!
                                      [uuid? item-uid]))
    (DELETE "/" [] (validate-and-handle service/close-warranty!
                                        [uuid? item-uid]))
    (POST "/warranty" {:keys [body]} (validate-and-make-warranty-decision!
                                      item-uid
                                      body)))
  (not-found {:status 404}))

(def router (handler/api routes))