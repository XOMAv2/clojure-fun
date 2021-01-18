(ns warehouse-service.services.warranty-service
   (:require [clj-http.client :as client]
             [clojure.data.json :as json]
             [config.core :refer [load-env]]
             [common-functions.helpers :refer [def-cb-service-call
                                               apply-cb-service-call]]
             [warehouse-service.services.warehouse-service :as warehouse]
             [common-functions.helpers :refer [create-response]])
  (:use [slingshot.slingshot :only [try+]]))

(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(def cb-warranty-request!
  (def-cb-service-call
    (fn [path request]
      (client/post path {:body (json/write-str request)
                         :headers {"Content-Type" "application/json"}}))))

(defn warranty-request!
  [order-item-uid reason]
  (try+ (let [available-count (warehouse/check-item-available-count order-item-uid)
              request {:reason reason
                       :availableCount available-count}
              path (str warranty-url "api/v1/warranty/" order-item-uid "/warranty")
              response (apply-cb-service-call cb-warranty-request! path request)
              json-body (:body response)
              map-body (json/read-str json-body :key-fn keyword)]
          (create-response 200 map-body))
        (catch [:status 404] {:keys [status body headers]}
          {:status 404 :body body :headers headers})
        (catch [:status 500] {:keys [body headers]}
          {:status 422 :body body :headers headers})
        (catch [:status 503] {:keys [status body headers]}
          {:status 422 :body body :headers headers})
        (catch Exception e
          (create-response 500 {:message (ex-message e)}))))