(ns store-service.services.warranty-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [common-functions.auth-service :refer [auth-request]]
            [config.core :refer [load-env]])
  (:use [slingshot.slingshot :only [try+]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(def name "store-service")
(def password "store-service")
(def auth-path (str warranty-url "api/v1/warranty/auth"))
(def access-token (atom (auth-request name password auth-path)))

(def cb-get-item-warranty-info!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}}))))))

(defn get-item-warranty-info!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-get-item-warranty-info! path)]
    response))