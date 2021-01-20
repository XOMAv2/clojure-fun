(ns session-service.core
  (:require [session-service.entities.users
             :refer [*db-spec* users-table-spec]
             :rename {*db-spec* *users-db-spec*}]
            [session-service.entities.codes
             :refer [*db-spec* codes-table-spec]
             :rename {*db-spec* *codes-db-spec*}]
            [common-functions.db :refer [create-table-if-not-exists!]]
            [session-service.repositories.users-repository :as rep]
            [ring.adapter.jetty :refer [run-jetty]]
            [common-functions.uuid :refer [uuid]]
            [config.core :refer [load-env]]
            [buddy.hashers :as hashers]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
            [session-service.routers.session-router :refer [router] :rename {router app-naked}]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(defn add-test-data! []
  (let [user {:name "Alex"
              :user_uid (uuid "6d2cb5a0-943c-4b96-9aa6-89eac7bdfd2b")
              :password_hash (hashers/derive "Alex" {:salt (:salt (load-env))})}]
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
  (create-table-if-not-exists! *users-db-spec* users-table-spec)
  (add-test-data!)
  (create-table-if-not-exists! *codes-db-spec* codes-table-spec)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))