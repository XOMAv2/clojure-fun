(ns warranty-service.services.warranty-service
  (:require [warranty-service.repositories.warranty-repository :as rep]
            [common-functions.helpers :refer [create-response]]
            [common-functions.time :refer [sql-timestamp-to-local-date-time]]
            [java-time :as time])
  (:use [clojure.set :only [rename-keys]]))

(defn get-warranty!
  "Получение строки с указанным item-uid из таблицы warranty через репозиторий."
  [item-uid]
  (try (let [warranty (rep/get-warranty! item-uid)]
         (if warranty
           (create-response 200
                            (rename-keys warranty {:item_uid :itemUid
                                                   :warranty_date :warrantyDate
                                                   :status :warrantyStatus}))
           (create-response 404
                            {:message "The warranty with the specified item_uid was not found."})))
       (catch Exception e (create-response 500  {:message (ex-message e)}))))

(defn start-warranty!
  "Добавление строки в таблицу warranty через репозиторий."
  [item-uid]
  (try (rep/add-warranty! {:item_uid item-uid
                           :status "ON_WARRANTY"
                           :warranty_date (time/sql-timestamp (time/local-date-time))})
       {:status 204}
       (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn close-warranty!
  "Удаление строки с указанным item-uid из таблицы warranty через репозиторий."
  [item-uid]
  (try (rep/delete-warranty! item-uid)
       {:status 204}
       (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn active-warranty?
  [warranty]
  (time/after? (:warranty_date warranty) (time/minus (time/local-date-time) (time/months 1))))

(defn warranty-decision!
  "Запрос решения по гарантии."
  [item-uid request]
  (try (let [warranty (rep/get-warranty! item-uid)]
         (if warranty
           (let [local-date-time (-> warranty
                                     (:warranty_date)
                                     (sql-timestamp-to-local-date-time))
                 warranty-local (assoc warranty :warranty_date local-date-time)
                 decision (cond
                            (and (active-warranty? warranty-local)
                                 (= (:status warranty) "ON_WARRANTY")
                                 (> (:availableCount request) 0)) "RETURN"
                            (and (active-warranty? warranty-local)
                                 (= (:status warranty) "ON_WARRANTY")
                                 (not (> (:availableCount request) 0))) "FIXING"
                            :else "REFUSE")]
             (rep/update-warranty! (:item_uid warranty)
                                   (-> warranty
                                       (assoc :comment (:reason request))
                                       (assoc :status (if (= decision "REFUSE")
                                                        "REMOVED_FROM_WARRANTY"
                                                        "USE_WARRANTY"))))
             (create-response 200
                              {:decision decision
                               :warrantyDate (:warranty_date warranty)}))
           (create-response 404
                            {:message (str "Warranty not found for itemUid '" item-uid "'")})))
       (catch Exception e (create-response 500 {:message (ex-message e)}))))