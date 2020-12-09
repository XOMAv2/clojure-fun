(ns store-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]))

(def warehouse-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warehouse-url env)))

(defn get-item-info!
  [item-uid]
  (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
        response (client/get path)]
    response))