(ns store-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [common-functions.helpers :refer [def-cb-service-call
                                              apply-cb-service-call]]
            [config.core :refer [load-env]]))

(def warehouse-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warehouse-url env)))

(def cb-get-item-info!
  (def-cb-service-call
    (fn [path]
      (client/get path))))

(defn get-item-info!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (apply-cb-service-call cb-get-item-info! path)]
    response))