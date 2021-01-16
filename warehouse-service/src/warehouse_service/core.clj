(ns warehouse-service.core
  (:require [warehouse-service.entities.items
             :refer [*db-spec* items-table-spec]
             :rename {*db-spec* *items-db-spec*}]
            [warehouse-service.entities.order-item
             :refer [*db-spec* order-item-table-spec]
             :rename {*db-spec* *order-item-db-spec*}]
            [common-functions.db :refer [create-table-if-not-exists!]]
            [ring.adapter.jetty :refer [run-jetty]]
            [warehouse-service.repositories.items-repository :as rep]
            [warehouse-service.routers.warehouse-router :refer [router] :rename {router app-naked}]
            [common-functions.middlewares :refer [remove-utf-8-from-header]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(defn add-test-data!
  []
  (let [item1 {:available_count 1000
               :model "Lego 8070"
               :size "M"}
        item2 {:available_count 1000
               :model "Lego 8880"
               :size "L"}
        item3 {:available_count 1000
               :model "Lego 42070"
               :size "L"}]
    (when (not (rep/get-item-by-model-and-size! (:model item1) (:size item1)))
      (rep/add-item! item1))
    (when (not (rep/get-item-by-model-and-size! (:model item2) (:size item2)))
      (rep/add-item! item2))
    (when (not (rep/get-item-by-model-and-size! (:model item3) (:size item3)))
      (rep/add-item! item3))))

; Middleware выполняются снизу вверх для запроса и сверху вниз для ответа.
(def app
  (-> app-naked
      ; Преобразует тело ответа в JSON.
      (wrap-json-response)
      (remove-utf-8-from-header)
      ; Преобразует тело запроса в словарь с ключами кейвордами.
      (wrap-json-body {:keywords? true})))

(defn -main
  [& args]
  (create-table-if-not-exists! *items-db-spec* items-table-spec)
  (add-test-data!)
  (create-table-if-not-exists! *order-item-db-spec* order-item-table-spec)
  (run-jetty app {:host (first args)
                  :port (Integer/parseInt (second args))
                  :join? false}))