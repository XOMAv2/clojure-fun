(ns warehouse-service.repositories.items-repository
  (:require [warehouse-service.entities.items :refer [*db-spec*]]
            [warehouse-service.repositories.order-item-repository :refer [get-item-id-by-order-item-uid]]
            [clojure.java.jdbc :refer [query execute! insert!]]
            [honeysql.core :refer [format call] :rename {format honey-eval
                                                         call honey-call}]
            [honeysql.helpers :as honey]))

(defn add-item!
  "Добавление строки в таблицу items."
  ([item]
   (add-item! *db-spec* item))
  ([db-spec item]
   (first (insert! db-spec :items item))))

(defn get-item-by-order-item-uid
  ([order-item-uid]
   (get-item-by-order-item-uid *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (let [item-id (get-item-id-by-order-item-uid order-item-uid)]
     (when (not= item-id nil)
       (first (query db-spec (-> (honey/from :items)
                                 (honey/where [:= item-id :id])
                                 (honey/select :*)
                                 (honey-eval))))))))

(defn get-item-by-model-and-size
  ([model size]
   (get-item-by-model-and-size *db-spec* model size))
  ([db-spec model size]
   (first (query db-spec (-> (honey/from :items)
                             (honey/where [:= model :model]
                                          [:= size :size])
                             (honey/select :*)
                             (honey-eval))))))

(defn take-one-item!
  "Уменьшает значение available_count на 1 для строки с указанным id в таблице items."
  ([item-id]
   (take-one-item! *db-spec* item-id))
  ([db-spec item-id]
   (execute! db-spec (-> (honey/update :items)
                         (honey/sset {:available_count (honey-call :- :available_count 1)})
                         (honey/where [:= item-id :id])
                         (honey-eval)))))

(defn return-one-item!
  "Увеличивает значение available_count на 1 для строки в таблице items с id,
  равным item-id из таблциы order-item из строки с указанным order-item-uid."
  ([order-item-uid]
   (return-one-item! *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (let [item-id (get-item-id-by-order-item-uid order-item-uid)]
     (when (not= item-id nil)
       (execute! db-spec (-> (honey/update :items)
                             (honey/sset {:available_count (honey-call :+ :available_count 1)})
                             (honey/where [:= item-id :id])
                             (honey-eval)))))))