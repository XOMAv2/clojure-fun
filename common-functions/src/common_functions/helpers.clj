(ns common-functions.helpers
  (:require [clojure.spec.alpha :as s]
            [common-functions.circuit-breaker :as cb])
  (:use [slingshot.slingshot :only [throw+ try+]]))

(defn create-response
  ([status body]
   {:status status
    :body body})
  ([status body content-type]
   {:status status
    :headers {"Content-Type" content-type}
    :body body}))

(defn if-assoc
  [map condition key val]
  (if condition
    (assoc map key val)
    map))

(defn assoc-if-absent
  [map key val]
  (if (contains? map key)
    map
    (assoc map key val)))

(defn validate-and-handle
  "Функция принимает на вход обработчик route'а и пары вида
   [валидатор валидируемое_значение]. Если вознкли ошибки валидации,
   то будет возвращён ассоциативный массив
   {:status 400 :body {:message [\"Ошибка 1\" \"Ошибка 2\" ...]}}.
   Иначе будет вызван обработчик с аргументами-валидируемыми_значениями."
  [handler & pairs]
  (let [errors (reduce #(let [coll %
                              spec (first %2)
                              x (second %2)]
                          (if (s/valid? spec x)
                            coll
                            (conj coll (s/explain-str spec x))))
                       []
                       pairs)]
    (if (= errors [])
      (apply handler (reduce #(conj % (second %2)) [] pairs))
      {:status 400 :body {:message errors}})))

(defn def-cb-service-call
  [call-function]
  (cb/make-circuit-breaker
   (fn [& args]
     (try+ {:result :ok
            :value {:response (apply call-function args)}}
           (catch #(#{404 422 500} (:status %)) {:as call-response}
             {:result :ok
              :value {:should-throw? true
                      :response call-response}})
           (catch [:status 503] _
             {:result :soft-failure})
           (catch Exception _
             {:result :soft-failure})))
   {:max-retries 3 :retry-after-ms 1000}))

(defn apply-cb-service-call
  [cb-call-function & args]
  (let [res (apply cb-call-function args)
        status (:result res)
        value (:value res)
        should-throw? (:should-throw? value)
        response (:response value)]
    (if (= status :ok)
      (if should-throw?
        (throw+ response)
        response)
      (throw+ {:status 503
               :body {:message "The service is not available."}}))))