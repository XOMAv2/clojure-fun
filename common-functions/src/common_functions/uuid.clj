(ns common-functions.uuid)

(defn uuid [str]
  (try
    (java.util.UUID/fromString str)
    (catch Exception _)))

(defn random-uuid []
  (java.util.UUID/randomUUID))

(defn json-write-uuid
  "Функция для преобразования uuid к строке во время парсинга мапы к json."
  [key value]
  (if (uuid? value) (str value) value))