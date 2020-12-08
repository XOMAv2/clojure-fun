(ns order-service.services.warranty-service
  (:require [clj-http.client :as client]
            [config.core :refer [load-env]]
            ;[order-service.helpers.subroutines :refer [create-response]]
            )
  ;(:use [slingshot.slingshot :only [try+]])
  )
    
(def warranty-url (let [config (load-env)
                        env-type (:env-type config)
                        env (env-type (:env config))]
                    (:warranty-url env)))

(defn start-warranty!
  [item-uid]
  ;(try+
   (let [path (str warranty-url "api/v1/warranty/" item-uid)
         response (client/post path)]
     response)
   ;(catch [:status 500] {:keys [body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch Exception e
   ;  (create-response 500 {:message (ex-message e)})))
  )

(defn stop-warranty!
  [item-uid]
  ;(try+
   (let [path (str warranty-url "api/v1/warranty/" item-uid)
         response (client/delete path)]
     response)
   ;(catch [:status 500] {:keys [body headers]}
   ;  {:status 422, :body body, :headers headers})
   ;(catch Exception e
   ;  (create-response 500 {:message (ex-message e)})))
  )