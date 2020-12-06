(ns warehouse-service.helpers.subroutines)

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