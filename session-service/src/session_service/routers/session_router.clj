(ns session-service.routers.session-router
  (:require [compojure.core :refer [POST defroutes context]]
            [compojure.handler :as handler]
            [compojure.route :refer [not-found]]
            [clojure.spec.alpha :as s]
            [compojure.coercions :refer [as-uuid]]
            [common-functions.helpers :refer [validate-and-handle]]
            [session-service.services.session-service :as service])
  (:use [clojure.set :only [rename-keys]]))

(s/def ::name (s/and string? #(<= (count %) 255)))
(s/def ::password string?)
(s/def ::callback string?)
(s/def ::clientId int?)

(s/def ::auth-body (s/keys :req-un [::name
                                    ::password
                                    ::clientId
                                    ::callback]))

(def ^:private uuid-pattern
  #"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
(s/def ::code (s/and string? #(re-matches uuid-pattern %)))
(s/def ::clientSecret string?)

(s/def ::token-body (s/keys :req-un [::code
                                     ::clientId
                                     ::clientSecret]))

(defroutes routes
  (context "/api/v1/session/oauth2" []
    (POST "/auth" {:keys [body]}
      (validate-and-handle #(service/auth (rename-keys % {:clientId :client-id}))
                           [::auth-body body]))
    (POST "/token" {:keys [body]}
      (validate-and-handle #(service/code->jwt
                             (-> %
                                 (update :code as-uuid)
                                 (rename-keys {:clientId :client-id
                                               :clientSecret :client-secret})))
                           [::token-body body]))
    (POST "/refresh" {{refresh-token :refreshToken} :body}
      (validate-and-handle service/refresh [string? refresh-token]))
    (POST "/check" {{access-token :accessToken} :body}
      (validate-and-handle service/check [string? access-token])))
  (not-found {:status 404}))

(def router (handler/api routes))