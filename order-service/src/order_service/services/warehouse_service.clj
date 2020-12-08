(ns order-service.services.warehouse-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            [clojure.data.json :as json]
            [order-service.helpers.subroutines :refer [json-write-uuid]]
            ;[order-service.helpers.subroutines :refer [create-response]]
            )
  ;(:use [slingshot.slingshot :only [try+]])
  )

(def warehouse-url (let [config (load-env)
                         env-type (:env-type config)
                         env (env-type (:env config))]
                     (:warehouse-url env)))

(defn take-item!
  [order-uid model size]
  ;(try+
   (let [request {:orderUid order-uid
                  :model model
                  :size size}
         path (str warehouse-url "api/v1/warehouse")
         response (client/post path
                               {:body (json/write-str request
                                                      :value-fn json-write-uuid)})]
     response)
   ;(catch [:status 404] {:keys [status body headers]}
   ;  {:status 404, :body body, :headers headers})
   ;(catch [:status 500] {:keys [body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch Exception e
   ;  (create-response 500 {:message (ex-message e)})))
  )

(take-item! (java.util.UUID/randomUUID) "model" "size")

(defn return-item!
  [item-uid]
  ;(try+
   (let [path (str warehouse-url "api/v1/warehouse/" item-uid)
         response (client/delete path)]
     response)
   ;(catch [:status 500] {:keys [body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch Exception e
   ;  (create-response 500 {:message (ex-message e)})))
  )

(defn use-warranty-item!
  [item-uid request]
  ;(try+
   (let [path (str warehouse-url "api/v1/warehouse/" item-uid "/warranty")
         response (client/post path {:body (json/write-str request)})]
     response)
   ;(catch [:status 422] {:keys [status body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch [:status 500] {:keys [body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch Exception e
   ;  (create-response 500 {:message (ex-message e)})))
  )