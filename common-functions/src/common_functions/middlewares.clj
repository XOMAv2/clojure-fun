(ns common-functions.middlewares)

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