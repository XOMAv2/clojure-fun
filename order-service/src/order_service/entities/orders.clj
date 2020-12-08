(ns order-service.entities.orders
  (:require [clojure.java.jdbc :as jdbc]
            [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def orders-table-spec {:name :orders
                       :columns [[:id :int :primary :key :generated :always :as :identity]
                                 [:item_uid :uuid :not :null]
                                 [:order_date :timestamp :not :null]
                                 [:order_uid :uuid :not :null :unique]
                                 [:status "VARCHAR(255)" :not :null]
                                 [:user_uid :uuid :not :null]]})

(defn create-orders-table!
  "Создание таблицы orders, если она не существует."
  []
  (jdbc/db-do-commands *db-spec*
                       (jdbc/create-table-ddl (:name orders-table-spec)
                                              (:columns orders-table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))