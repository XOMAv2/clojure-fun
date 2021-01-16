(ns store-service.core
  (:require [store-service.entities.users :refer [create-users-table!]]
            [store-service.repositories.users-repository :as rep]
            [ring.adapter.jetty :refer [run-jetty]]
            [store-service.helpers.subroutines :refer [uuid remove-utf-8-from-header]]
            [store-service.routers.users-router :refer [router] :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(defn add-test-data!
  []
  (let [user {:name "Alex"
              :user_uid (uuid "6d2cb5a0-943c-4b96-9aa6-89eac7bdfd2b")}]
    (when (not (rep/get-user-by-user-uid! (:user_uid user)))
      (rep/add-user! user))))

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
  (create-users-table!)
  (add-test-data!)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))