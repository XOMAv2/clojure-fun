(ns order-service.services.warranty-service
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

(def name "order-service")
(def password "order-service")
(def auth-path (str warranty-url "api/v1/warranty/auth"))
(def access-token (atom (auth-request name password auth-path)))

(def cb-start-warranty!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/post path {"Authorization" (str "Bearer " @access-token)})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/post path {"Authorization" (str "Bearer " @access-token)}))))))

(defn start-warranty!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-start-warranty! path)]
    response))

(def cb-stop-warranty!
  (def-cb-service-call
    (fn [path]
      (try+
       (client/delete path {"Authorization" (str "Bearer " @access-token)})
       (catch [:status 401] _
         (swap! access-token (fn [x] (auth-request name password auth-path)))
         (client/delete path {"Authorization" (str "Bearer " @access-token)}))))))

(defn stop-warranty!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-stop-warranty! path)]
    response))