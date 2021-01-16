(ns warranty-service.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [warranty-service.routers.warranty-router :refer [router] :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
            [warranty-service.entities.warranty :refer [*db-spec* warranty-table-spec]]
            [common-functions.db :refer [create-table-if-not-exists!]])
  (:gen-class))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      ;(wrap-validate-body)
      (remove-utf-8-from-header)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})))

(defn -main
  [& args]
  (create-table-if-not-exists! *db-spec* warranty-table-spec)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))