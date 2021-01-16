(ns common-functions.db
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-table-if-not-exists!
  "Создание таблицы, если она не существует. Мапа table-spec
   должна содержать ключ :name с именем таблицы и ключ :columns
   с описанием колонок. Например,
   {:name :orders
    :columns [[:id :int :primary :key :generated :always :as :identity]
              [:item_uid :uuid :not :null]
              [:order_date :timestamp :not :null]
              [:order_uid :uuid :not :null :unique]
              [:status \"VARCHAR(255)\" :not :null]
              [:user_uid :uuid :not :null]]}"
  [db-spec table-spec]
  (jdbc/db-do-commands db-spec
                       (jdbc/create-table-ddl (:name table-spec)
                                              (:columns table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))