(ns order-service.repositories.orders-repository
  (:require [order-service.entities.orders :refer [*db-spec*]]
            [clojure.java.jdbc :refer [query execute! insert!]]
            [honeysql.core :refer [format call] :rename {format honey-eval
                                                         call honey-call}]
            [honeysql.helpers :as honey]))
    
(defn add-order!
  "Добавление строки в таблицу orders."
  ([order]
   (add-order! *db-spec* order))
  ([db-spec order]
   (first (insert! db-spec :orders order))))

(defn get-order-by-order-uid
  ([order-uid]
   (get-order-by-order-uid *db-spec* order-uid))
  ([db-spec order-uid]
   (first (query db-spec (-> (honey/from :orders)
                             (honey/where [:= order-uid :order_uid])
                             (honey/select :*)
                             (honey-eval))))))

(defn get-order-by-user-uid-and-order-uid
  ([user-uid order-uid]
   (get-order-by-user-uid-and-order-uid *db-spec*
                                        user-uid
                                        order-uid))
  ([db-spec user-uid order-uid]
   (first (query db-spec (-> (honey/from :orders)
                             (honey/where [:= user-uid :user_uid]
                                          [:= order-uid :order_uid])
                             (honey/select :*)
                             (honey-eval))))))

(defn get-orders-by-user-uid
  ([user-uid]
   (get-orders-by-user-uid *db-spec* user-uid))
  ([db-spec user-uid]
   (query db-spec (-> (honey/from :orders)
                      (honey/where [:= user-uid :user_uid])
                      (honey/select :*)
                      (honey-eval)))))

(defn delete-order!
  "Удаление строки с указанным order-uid из таблицы orders."
  ([order-uid]
   (delete-order! *db-spec* order-uid))
  ([db-spec order-uid]
   (execute! db-spec (-> (honey/delete-from :orders)
                         (honey/where [:= order-uid :order_uid])
                         (honey-eval)))))