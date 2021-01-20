(ns session-service.entities.codes
  (:require [config.core :refer [load-env]]))

(def ^:dynamic *db-spec*
  "Ассоциативный массив для подключения к бд с возможностью
   динамического связывания при помощи binding."
  (let [config (load-env)
        env-type (:env-type config)
        env (env-type (:env config))]
    (:db-spec env)))

(def codes-table-spec {:name :codes
                       :columns [[:id :int :primary :key :generated :always :as :identity]
                                 [:client_id :int :not :null :unique]
                                 [:access_token :text :not :null]
                                 [:refresh_token :text :not :null]
                                 [:code :uuid :not :null :unique]
                                 [:exp :timestamp :not :null]]})