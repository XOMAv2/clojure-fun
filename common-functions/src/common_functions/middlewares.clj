(ns common-functions.middlewares
  (:require [common-functions.helpers :refer [create-response]]
            [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:use [slingshot.slingshot :only [try+]]))

(defn remove-utf-8-from-header
  "Middleware для удаления строки \"charset=utf-8\" из заголовка \"Content-Type\"
   при возвращении json'а."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (contains? (:headers response) "Content-Type")
               (= ((:headers response) "Content-Type")
                  "application/json; charset=utf-8"))
        (assoc-in response [:headers "Content-Type"] "application/json")
        response))))

(defn oauth2-authorization
  "Middleware для проверки авторизационного токена, выданного по протоколу
   OAuth2, через сторонний авторизационный сервис.
   Аргументы функции:
   check-path - путь, по которому будет отправлен POST-запрос с телом
                {\"accessToken\": \"токен_без_Bearer\"}, где токе_без_Bearer
                вытаскивается из содержимого заголовка \"Authorization\".
   claims-handler - функция для обработки claim'ов, которые находились в токене.
                    Может отсутствовать. Если присутствует, то должна принимать
                    на вход два параметры - это дессериализованные claim'ы и
                    request, а возвращать request."
  ([handler check-path]
   (oauth2-authorization handler check-path (fn [claims request] request)))
  ([handler check-path claims-handler]
  (fn [request]
    (try+
     (if-let [auth-header (get (:headers request) "authorization")]
       (let [token (str/replace-first auth-header #"Bearer " "")
             response (client/post check-path {:body (str "{\"accessToken\": \"" token "\"}")
                                               :headers {"Content-Type" "application/json"}})
             claims (json/read-str (:body response) :key-fn keyword)
             request (claims-handler claims request)]
         (handler request))
       (create-response 401 "Authorization header is missing"))
     (catch [:status 401] {:as response}
       response)
     (catch java.net.ConnectException _
       (create-response 422 "Authorization service is not available."))
     (catch #(#{500 503} (:status %)) _
       (create-response 422 "Authorization service is not available."))
     (catch Exception e
       (create-response 500 (ex-message e)))))))