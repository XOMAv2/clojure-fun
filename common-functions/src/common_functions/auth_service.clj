(ns common-functions.auth-service
  (:require [config.core :refer [load-env]]
            [clojure.string :as str]
            [common-functions.base64 :refer [base64->str str->base64str]]
            [common-functions.helpers :refer [create-response]]
            [clj-time.core :as t]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [buddy.hashers :as hashers]))

(def private-key (keys/private-key "jwtRS256.key"))

(def salt {:salt (:salt (load-env))})

(def users
  [["warranty-service" (hashers/derive "warranty-service" salt)]
   ["warehouse-service" (hashers/derive "warehouse-service" salt)]
   ["order-service" (hashers/derive "order-service" salt)]
   ["store-service" (hashers/derive "store-service" salt)]])

(defn authorization-service-func
  "Выдача пользователю токена доступа через его аутентификацию.
   Код 401 означает, что в запросе отсутствует заголовок \"Authorization\".
   Код 418 означает, что пользователь не смог пройти авторизацию.
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
      (let [auth-header-base64 (str/replace-first auth-header-base64 #"Basic " "")
            auth-header (base64->str auth-header-base64)
            [name password] (str/split auth-header #":")]
        (if (not (or (empty? name) (empty? password)))
          (if (user-authenticated? name password)
            (let [claims {:exp (t/plus (t/now) (t/minutes 30))}
                  access-token (jwt/sign claims private-key {:alg :rs256})]
              (create-response 200 {:accessToken access-token}))
            (create-response 418 {:message "Invalid name or password."}))
          (create-response 418 {:message "Name or password is empty."})))
      (create-response 401 {:message "Authorization header is missing"}))
    (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn auth
  [auth-header-base64]
  (authorization-service-func auth-header-base64
                              (fn [name password]
                                (let [password-hash (hashers/derive password salt)]
                                  (some #(= % [name password-hash]) users)))
                              private-key))

(defn auth-request
  "Запрос авторизации одного микросервиса у другого."
  [name password auth-path]
  (let [header (str->base64str (str name ":" password))
        header (str "Basic " header)
        response (client/post auth-path {:headers {"Authorization" header}})
        claims (json/read-str (:body response) :key-fn keyword)]
    (:accessToken claims)))