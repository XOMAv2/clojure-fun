(ns warranty-service.repositories.warranty-repository
  (:require [warranty-service.entities.warranty :refer [*db-spec*]]
            [clojure.java.jdbc :refer [query execute! insert!]]
            [honeysql.core :refer [format] :rename {format honey-eval}]
            [honeysql.helpers :as honey]))

(defn get-warranty
  "Получение строки с указанным item-uid из таблицы warranty."
  ([item-uid]
   (get-warranty *db-spec* item-uid))
  ([db-spec item-uid]
   (first (query db-spec (-> (honey/from :warranty)
                             (honey/where [:= item-uid :item_uid])
                             (honey/select :*)
                             honey-eval)))))

(defn delete-warranty!
  "Удаление строки с указанным item-uid из таблицы warranty."
  ([item-uid]
   (delete-warranty! *db-spec* item-uid))
  ([db-spec item-uid]
   (execute! db-spec (-> (honey/delete-from :warranty)
                         (honey/where [:= item-uid :item_uid])
                         honey-eval))))

(defn add-warranty!
  "Добавление строки в таблицу warranty."
  ([warranty]
   (add-warranty! *db-spec* warranty))
  ([db-spec warranty]
   (first (insert! db-spec :warranty warranty))))

(defn update-warranty!
  "Обновление строки с указанным item-uid в таблице warranty."
  ([item-uid warranty]
   (update-warranty! *db-spec* item-uid warranty))
  ([db-spec item-uid warranty]
   (if (not= nil (get-warranty db-spec item-uid))
     (execute! db-spec (-> (honey/update :warranty)
                           (honey/sset (-> warranty
                                           (dissoc :id)))
                           (honey/where [:= (:id warranty) :id])
                           honey-eval))
     '(0))))