(ns store-service.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [config.core :refer [load-env]]
            [common-functions.middlewares :refer [remove-utf-8-from-header authorize]]
            [store-service.routers.users-router :refer [router] :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(def token-check-url (let [config (load-env)
                           env-type (:env-type config)
                           env (env-type (:env config))]
                       (str (:session-url env) "api/v1/session/oauth2/check")))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      (remove-utf-8-from-header)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})
      (authorize token-check-url
                 (fn [claims request]
                   (let [params (:params request)
                         params (assoc params :claims claims)]
                     (assoc request :params params))))))

(let [config (load-env)
      env-type (:env-type config)
      env (env-type (:env config))]
  (:db-spec env))

(defn -main
  [& args]
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))