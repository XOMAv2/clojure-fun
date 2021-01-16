(ns order-service.core
  (:require [order-service.entities.orders :refer [create-orders-table!]]
            [ring.adapter.jetty :refer [run-jetty]]
            [order-service.routers.orders-router :refer [router] :rename {router app-naked}]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      (remove-utf-8-from-header)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})))

(defn -main
  [& args]
  (create-orders-table!)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))