(ns warranty-service.entities.warranty
  (:require [clojure.java.jdbc :as jdbc]
            [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def warranty-table-spec {:name :warranty
                          :columns [[:id :int :primary :key :generated :always :as :identity]
                                    [:comment "VARCHAR(1024)"]
                                    [:item_uid :uuid :not :null :unique]
                                    [:status "VARCHAR(255)" :not :null]
                                    [:warranty_date :timestamp :not :null]]})

(defn create-warranty-table!
  "Создание таблицы warranty, если она не существует."
  []
  (jdbc/db-do-commands *db-spec*
                       (jdbc/create-table-ddl (:name warranty-table-spec)
                                              (:columns warranty-table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))