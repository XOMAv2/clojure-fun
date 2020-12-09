(ns store-service.services.users-service
  (:require [store-service.repositories.users-repository :as rep]))

(defn get-user-by-user-uid!
  [user-uid]
  (rep/get-user-by-user-uid! user-uid))

(defn user-exists?
  [user-uid]
  (-> user-uid
      (rep/get-user-by-user-uid!)
      (not= nil)))