(ns order-service.services.warranty-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [config.core :refer [load-env]]))
    
(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(def cb-start-warranty!
  (def-cb-service-call
    (fn [path]
      (client/post path))))

(defn start-warranty!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-start-warranty! path)]
    response))

(def cb-stop-warranty!
  (def-cb-service-call
    (fn [path]
      (client/delete path))))

(defn stop-warranty!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-stop-warranty! path)]
    response))