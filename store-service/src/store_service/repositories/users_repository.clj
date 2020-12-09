(ns store-service.repositories.users-repository
  (:require [store-service.entities.users :refer [*db-spec*]]
            [clojure.java.jdbc :refer [query insert!]]
            [honeysql.core :refer [format] :rename {format honey-eval}]
            [honeysql.helpers :as honey]))

(defn get-user-by-user-uid!
  ([user-uid]
   (get-user-by-user-uid! *db-spec* user-uid))
  ([db-spec user-uid]
   (first (query db-spec (-> (honey/from :users)
                             (honey/where [:= user-uid :user_uid])
                             (honey/select :*)
                             (honey-eval))))))

(defn add-user!
  "Добавление строки в таблицу users."
  ([user]
   (add-user! *db-spec* user))
  ([db-spec user]
   (first (insert! db-spec :users user))))

