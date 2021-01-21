(ns common-functions.helpers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [buddy.sign.jwt :as jwt]
            [common-functions.base64 :refer [base64->str]]
            [clj-time.core :as t]
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

(defn authorization-service-func
  "Аутентификация пользователя для выдачи ему токена доступа.
   Код 401 означает, что в запросе отсутствует заголовок \"Authorization\".
   Код 422 означает, что пользователь не смог пройти авторизацию.
   Код 200 означает, что токен выдан и помещён в тело в поле \"accessToken\".
   Аргументы функции:
   * auth-header-base64 - содержимое заголовка \"Authorization\" запроса.
   * user-authenticated? - функция, которая по name и password пользователя
                           определяет, авторизирован он или нет.
                           (user-authenticated? name password)
   * private-key - секретный ключ сервиса."
  [auth-header-base64 user-authenticated? private-key]
  (try
    (if auth-header-base64
      (let [auth-header (base64->str auth-header-base64)
            [name password] (str/split (str/replace-first auth-header #"Basic " "") #":")]
        (if (user-authenticated? name password)
          (let [claims {:exp (t/plus (t/now) (t/minutes 30))}
                access-token (jwt/sign claims private-key {:alg :rs256})]
            (create-response 200 {:accessToken access-token}))
          (create-response 422 "Invalid name or password.")))
      (create-response 401 "Authorization header is missing"))
    (catch Exception e (create-response 500 (ex-message e)))))