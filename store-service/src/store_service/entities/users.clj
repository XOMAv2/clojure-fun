(ns store-service.entities.users
  (:require [clojure.java.jdbc :as jdbc]
            [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def users-table-spec {:name :users
                       :columns [[:id :int :primary :key :generated :always :as :identity]
                                 [:name "VARCHAR(255)" :not :null :unique]
                                 [:user_uid :uuid :not :null :unique]]})

(defn create-users-table!
  "Создание таблицы users, если она не существует."
  []
  (jdbc/db-do-commands *db-spec*
                       (jdbc/create-table-ddl (:name users-table-spec)
                                              (:columns users-table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))