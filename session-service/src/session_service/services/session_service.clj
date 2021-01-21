(ns session-service.services.session-service
  (:require [config.core :refer [load-env]]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [buddy.hashers :as hashers]
            [common-functions.uuid :refer [random-uuid]]
            [common-functions.helpers :refer [create-response]]
            [session-service.repositories.users-repository :as u-rep]
            [session-service.repositories.codes-repository :as c-rep]
            [clj-time.core :as t]
            [common-functions.time :refer [sql-timestamp-to-local-date-time]]
            [java-time :as time]))

;; Для генерации ключей использовались команды:
;; ssh-keygen -t rsa -b 4096 -m PEM -f jwtRS256.key
;; Passphrase добавлять не надо.
;; openssl rsa -in jwtRS256.key -pubout -outform PEM -out jwtRS256.key.pub

;; Для генерации соли использовалась форма:
;; (bytes->hex (nonce/random-nonce 8))
;; Для её вызова необходимо добавить в проект зависимость
;; [buddy/buddy-core "1.9.0"]
;; и подключить к пространству имён
;; (:require [buddy.core.codecs :refer :all]
;;           [buddy.core.nonce :as nonce]

(def private-key (keys/private-key "jwtRS256.key"))
(def public-key (keys/public-key "jwtRS256.key.pub"))
(def clients
  (let [salt {:salt (:salt (load-env))}]
    [[111 (hashers/derive "password1" salt)]
     [222 (hashers/derive "password2" salt)]]))

(defn auth
  "Запрос аутентификации пользователя для получения клиентом
   авторизационного кода.
   Код 404 означает, что сервис не зарегистрирован на сервере.
   Код 422 означает, что пользователь не смог пройти авторизацию.
   Код 302 означает, что токены выданы, а авторизационный код помещён в
   заголовок Location после callback-пути в качестве квери-парамтера code."
  [{:keys [client-id] :as body}]
  (try
    (if (some #(= (first %) client-id) clients)
      (if-let [{user-uid :user_uid} (u-rep/get-user-by-name-and-password-hash!
                                     (:name body)
                                     (hashers/derive (:password body) {:salt (:salt (load-env))}))]
        (let [claims {:user-uid user-uid
                      :exp (t/plus (t/now) (t/days 30))}
              refresh-token (jwt/sign claims private-key {:alg :rs256})
              claims {:user-uid user-uid
                      :exp (t/plus (t/now) (t/minutes 30))}
              access-token (jwt/sign claims private-key {:alg :rs256})
              authorization-code (random-uuid)]
          (when-let [row (c-rep/get-row-by-client-id! client-id)]
            (c-rep/delete-row-by-code! (:code row)))
          (c-rep/add-code! {:client_id client-id
                            :access_token access-token
                            :refresh_token refresh-token
                            :code authorization-code
                            :exp (time/sql-timestamp (time/plus (time/local-date-time)
                                                                (time/days 1)))})
          {:status 302 :headers {"Location" (str (:callback body) "?code=" authorization-code)}})
        (create-response 422 {:message "Invalid name or password."}))
      (create-response 404 {:message "A client with a specific clientId was not found."}))
    (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn code->jwt
  "Обмен авторизационного кода на jwt-токены при условии
   успешной аутентификации клиента.
   Код 422 означает, что клиент не прошёл аутентификацию.
   Код 404 означает, что токены для указанного кода не найдены.
   Код 401 означает, что срок действия авторизационного кода истёк.
   Код 200 означает, что авторизация пройдена и токены будут помещены в тело ответа."
  [{:keys [code client-id client-secret]}]
  (try
    (if (let [salt {:salt (:salt (load-env))}
              client-secret-hash (hashers/derive client-secret salt)]
          (some #(= % [client-id client-secret-hash]) clients))
      (if-let [row (c-rep/get-row-by-code! code)]
        (do (c-rep/delete-row-by-code! code) ; Удаляем код из БД, так как он одноразовый.
            (if (-> row
                    (:exp)
                    (sql-timestamp-to-local-date-time)
                    (time/after? (time/local-date-time)))
              (create-response 200 {:accessToken (:access_token row)
                                    :refreshToken (:refresh_token row)})
              (create-response 401 {:message "Code is expired."})))
        (create-response 404 {:message (str "Tokens not found for code '" code "'.")}))
      (create-response 422 {:message "Invalid clientId or clientSecret."}))
    (catch Exception e (create-response 500 {:message (ex-message e)}))))

(defn refresh
  "Обновление токенов.
   Код 401 означает, что refresh-token не валиден.
   Код 200 означет, что токены успешно обновлены и помещены в тело ответа."
  [refresh-token]
  (try (let [{:keys [user-uid]} (jwt/unsign refresh-token public-key {:alg :rs256})
             claims {:user-uid user-uid
                     :exp (t/plus (t/now) (t/days 30))}
             refresh-token (jwt/sign claims private-key {:alg :rs256})
             claims {:user-uid user-uid
                     :exp (t/plus (t/now) (t/minutes 30))}
             access-token (jwt/sign claims private-key {:alg :rs256})]
         (create-response 200 {:refreshToken refresh-token :accessToken access-token}))
       (catch clojure.lang.ExceptionInfo e
         (create-response 401 {:message (ex-message e)}))
       (catch Exception e
         (create-response 500 {:message (ex-message e)}))))

(defn check
  "Подтверждение подлинности ваданного токена.
   Код 401 означает, что токен не валиден.
   Код 200 означет, что токен валиден и в тело помещены его клеймы."
  [access-token]
  ;(jwt/unsign token public-key {:alg :rs256 :now (t/minus (t/now) (t/seconds 5))})
  (try (create-response 200 (jwt/unsign access-token public-key {:alg :rs256}))
       (catch clojure.lang.ExceptionInfo e
         (create-response 401 {:message (ex-message e)}))
       (catch Exception e
         (create-response 500 {:message (ex-message e)}))))