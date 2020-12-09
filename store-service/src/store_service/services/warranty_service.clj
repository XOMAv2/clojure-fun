(ns store-service.services.warranty-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(defn get-item-warranty-info!
  [item-uid]
  (let [path (str warranty-url "api/v1/warranty/" item-uid)
        response (client/get path)]
    response))