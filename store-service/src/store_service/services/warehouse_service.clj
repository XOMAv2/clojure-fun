(ns store-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [common-functions.auth-service :refer [auth-request]]
            [config.core :refer [load-env]])
  (:use [slingshot.slingshot :only [try+]]))

(def warehouse-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warehouse-url env)))

(def name "store-service")
(def password "store-service")
(def auth-path (str warehouse-url "api/v1/warehouse/auth"))
(def access-token (atom (auth-request name password auth-path)))

(def cb-get-item-info!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/get path {:headers {"Authorization" (str "Bearer " @access-token)}}))))))

(defn get-item-info!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (apply-cb-service-call cb-get-item-info! path)]
    response))