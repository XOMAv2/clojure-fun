(ns warehouse-service.entities.items
  (:require [clojure.java.jdbc :as jdbc]
            [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def items-table-spec {:name :items
                       :columns [[:id :int :primary :key :generated :always :as :identity]
                                 [:available_count :int :not :null]
                                 [:model "VARCHAR(255)" :not :null]
                                 [:size "VARCHAR(255)" :not :null]]})

(defn create-items-table!
  "Создание таблицы items, если она не существует."
  []
  (jdbc/db-do-commands *db-spec*
                       (jdbc/create-table-ddl (:name items-table-spec)
                                              (:columns items-table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))