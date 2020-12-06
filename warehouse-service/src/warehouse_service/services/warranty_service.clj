(ns warehouse-service.services.warranty-service
   (:require [clj-http.client :as client]
             [clojure.data.json :as json]
             [config.core :refer [load-env]]
             [warehouse-service.services.warehouse-service :as warehouse]
             [warehouse-service.helpers.subroutines :refer [create-response]])
  (:use [slingshot.slingshot :only [try+]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(defn warranty-request!
  [order-item-uid reason]
  (try+ (let [available-count (warehouse/check-item-available-count order-item-uid)
              request {:reason reason
                       :availableCount available-count}
              path (str warranty-url "api/v1/warranty/" order-item-uid "/warranty")
              response (client/post path {:body (json/write-str request)})
              json-body (:body response)
              map-body (json/read-str json-body :key-fn keyword)]
          (create-response 200 map-body "application/json"))
        (catch [:status 404] {:keys [status body]}
          (create-response status (json/read-str body :key-fn keyword)))
        (catch [:status 500] {:keys [body]}
          (create-response 422 (json/read-str body :key-fn keyword)))
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))