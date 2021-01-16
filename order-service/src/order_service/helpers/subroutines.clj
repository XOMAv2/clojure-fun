(ns order-service.helpers.subroutines
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

(defn remove-utf-8-from-header
  "Middleware для удаления строки \"charset=utf-8\" из заголовка \"Content-Type\"
   при возвращении json'а."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (contains? (:headers response) "Content-Type")
               (= ((:headers response) "Content-Type")
                  "application/json; charset=utf-8"))
        (assoc-in response [:headers "Content-Type"] "application/json")
        response))))