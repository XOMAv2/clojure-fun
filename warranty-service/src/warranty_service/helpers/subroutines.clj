(ns warranty-service.helpers.subroutines
  (:require [java-time :as time]))

(defn create-response
  ([status body]
   {:status status
    :body body})
  ([status body content-type]
   {:status status
    :headers {"content-type" content-type}
    :body body}))

(defn if-assoc
  [map condition key val]
  (if condition
    (assoc map key val)
    map))

(defn assoc-if-absent
  [map key val]
  (if (contains? map key)
    map
    (assoc map key val)))

(defn uuid [str]
  (try
    (java.util.UUID/fromString str)
    (catch Exception _ nil)))

(defn random-uuid []
  (java.util.UUID/randomUUID))

(defn sql-timestamp-to-local-date-time
  [sql-timestamp]
  (-> sql-timestamp
      (.getTime)
      (time/instant)
      (.atZone (time/zone-id))
      (.toLocalDateTime)))