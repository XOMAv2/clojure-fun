(ns store-service.helpers.subroutines
  (:require [java-time :as time]))

(defn create-response
  ([status body]
   {:status status
    :body body})
  ([status body content-type]
   {:status status
    :headers {"Content-Type" content-type}
    :body body}))

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

(defn json-write-uuid
  "Функция для преобразования uuid к строке во время парсинга мапы к json."
  [key value]
  (if (uuid? value) (str value) value))