(ns warehouse-service.repositories.order-item-repository
  (:require [warehouse-service.entities.order-item :refer [*db-spec*]]
            [clojure.java.jdbc :refer [query execute!]]
            [honeysql.core :refer [format] :rename {format honey-eval}]
            [honeysql.helpers :as honey]))

(defn get-item-id-by-order-item-uid!
  ([order-item-uid]
   (get-item-id-by-order-item-uid! *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (let [result-map (first (query db-spec (-> (honey/from :order_item)
                                              (honey/where [:= order-item-uid :order_item_uid])
                                              (honey/select :item_id)
                                              (honey-eval))))]
     (when (not= result-map nil)
       (:item_id result-map)))))

(defn add-order-item!
  "Добавление строки в таблицу order-item.
   В случае успеха будет возвращена последовательность с 
   единственным элементом - количеством затронутых строк."
  ([order-item]
   (add-order-item! *db-spec* order-item))
  ([db-spec order-item]
   (execute! db-spec (-> (honey/insert-into :order-item)
                         (honey/values [order-item])
                         (honey-eval)))))

(defn cancel-order-item!
  "Изменение значение canceled на true в строки с указанным order-item-uid в таблице order-item."
  ([order-item-uid]
   (cancel-order-item! *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (execute! db-spec (-> (honey/update :order-item)
                         (honey/sset {:canceled true})
                         (honey/where [:= order-item-uid :order_item_uid])
                         (honey-eval)))))
(defn open-order-item!
  "Изменение значение canceled на false в строки с указанным order-item-uid в таблице order-item."
  ([order-item-uid]
   (open-order-item! *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (execute! db-spec (-> (honey/update :order-item)
                         (honey/sset {:canceled false})
                         (honey/where [:= order-item-uid :order_item_uid])
                         (honey-eval)))))

(defn delete-order-item!
  "Удаление строки с указанным order-item-uid в таблице order-item."
  ([order-item-uid]
   (delete-order-item! *db-spec* order-item-uid))
  ([db-spec order-item-uid]
   (execute! db-spec (-> (honey/delete-from :order-item)
                         (honey/where [:= order-item-uid :order_item_uid])
                         (honey-eval)))))