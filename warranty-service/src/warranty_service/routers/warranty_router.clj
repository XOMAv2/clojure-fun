(ns warranty-service.routers.warranty-router
  (:require [compojure.core :refer [GET POST DELETE
                                    defroutes context routes wrap-routes]]
            [compojure.coercions :refer [as-uuid]]
            [compojure.handler :as handler]
            [compojure.route :refer [not-found]]
            [common-functions.auth-service :as auth-service]
            [buddy.core.keys :as keys]
            [common-functions.middlewares :refer [jwt-authorization]]
            [clojure.spec.alpha :as s]
            [common-functions.helpers :refer [valid?-handle]]
            [warranty-service.services.warranty-service :as service]))

(s/def ::reason string?)
(s/def ::availableCount int?)

(s/def ::request-warranty-decision (s/keys :req-un [::reason
                                                    ::availableCount]))

(defn validate-and-make-warranty-decision!
  [item-uid body]
  (valid?-handle service/warranty-decision!
                 [uuid? item-uid]
                 [::request-warranty-decision body]))

(defroutes public-routes
  (POST "/api/v1/warranty/auth" {{auth "authorization"} :headers}
    (auth-service/auth auth)))

(defroutes private-routes
  (context "/api/v1/warranty/:item-uid" [item-uid :<< as-uuid]
    (GET "/" [] (valid?-handle service/get-warranty!
                               [uuid? item-uid]))
    (POST "/" [] (valid?-handle service/start-warranty!
                                [uuid? item-uid]))
    (DELETE "/" [] (valid?-handle service/close-warranty!
                                  [uuid? item-uid]))
    (POST "/warranty" {:keys [body]} (validate-and-make-warranty-decision!
                                      item-uid
                                      body))))

(def router (handler/api (routes public-routes
                                 (wrap-routes private-routes
                                              jwt-authorization
                                              (keys/public-key "jwtRS256.key.pub"))
                                 (not-found {:status 404}))))