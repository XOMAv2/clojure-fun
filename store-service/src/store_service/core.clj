(ns store-service.core
  (:require [store-service.entities.users :refer [*db-spec* users-table-spec]]
            [common-functions.db :refer [create-table-if-not-exists!]]
            [store-service.repositories.users-repository :as rep]
            [ring.adapter.jetty :refer [run-jetty]]
            [common-functions.uuid :refer [uuid]]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
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
  (create-table-if-not-exists! *db-spec* users-table-spec)
  (add-test-data!)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))