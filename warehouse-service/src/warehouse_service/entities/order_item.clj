(ns warehouse-service.entities.order-item
  (:require [clojure.java.jdbc :as jdbc]
            [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def order-item-table-spec {:name :order_item
                            :columns [[:id :int :primary :key :generated :always :as :identity]
                                      [:canceled :boolean]
                                      [:order_item_uid :uuid :not :null :unique]
                                      [:order_uid :uuid :not :null]
                                      [:item_id :int "CONSTRAINT fk_order_item_item_id REFERENCES items"]]})

(defn create-order-item-table!
  "Создание таблицы order-item, если она не существует."
  []
  (jdbc/db-do-commands *db-spec*
                       (jdbc/create-table-ddl (:name order-item-table-spec)
                                              (:columns order-item-table-spec)
                                              ; IF NOT EXISTS
                                              {:conditional? true})))