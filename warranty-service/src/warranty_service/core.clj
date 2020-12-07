(ns warranty-service.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [warranty-service.routers.warranty-router :refer [router] :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [warranty-service.entities.warranty :refer [create-warranty-table!]])
  (:gen-class))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      ;(wrap-validate-body)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})))

(defn -main
  [& args]
  (create-warranty-table!)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))