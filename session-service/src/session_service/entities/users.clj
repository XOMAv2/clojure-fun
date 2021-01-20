(ns session-service.entities.users
  (:require [config.core :refer [load-env]]))

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
                                 [:user_uid :uuid :not :null :unique]
                                 [:password_hash :text :not :null]]})