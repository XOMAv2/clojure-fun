(ns common-functions.time
  (:require [java-time :as time]))

(defn sql-timestamp-to-local-date-time
  [sql-timestamp]
  (-> sql-timestamp
      (.getTime)
      (time/instant)
      (.atZone (time/zone-id))
      (.toLocalDateTime)))