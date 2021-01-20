(ns session-service.repositories.codes-repository
  (:require [session-service.entities.codes :refer [*db-spec*]]
            [clojure.java.jdbc :refer [query insert! execute!]]
            [honeysql.core :refer [format] :rename {format honey-eval}]
            [honeysql.helpers :as honey]))

(defn get-row-by-code!
  ([code]
   (get-row-by-code! *db-spec* code))
  ([db-spec code]
   (first (query db-spec (-> (honey/from :codes)
                             (honey/where [:= code :code])
                             (honey/select :*)
                             (honey-eval))))))

(defn get-row-by-client-id!
  ([client-id]
   (get-row-by-client-id! *db-spec* client-id))
  ([db-spec client-id]
   (first (query db-spec (-> (honey/from :codes)
                             (honey/where [:= client-id :client_id])
                             (honey/select :*)
                             (honey-eval))))))

(defn add-code!
  "Добавление строки в таблицу users.
   row - строка, содержащая все столбцы таблицы cods исключая id"
  ([row]
   (add-code! *db-spec* row))
  ([db-spec row]
   (first (insert! db-spec :codes row))))

(defn delete-row-by-code!
  "Удаление строки с указанным code из таблицы codes."
  ([code]
   (delete-row-by-code! *db-spec* code))
  ([db-spec code]
   (execute! db-spec (-> (honey/delete-from :codes)
                         (honey/where [:= code :code])
                         (honey-eval)))))