(ns store-service.services.warranty-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [config.core :refer [load-env]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(def cb-get-item-warranty-info!
  (def-cb-service-call
    (fn [path]
      (client/get path))))

(defn get-item-warranty-info!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (apply-cb-service-call cb-get-item-warranty-info! path)]
    response))