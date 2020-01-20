(ns newbiee.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.adapter.jetty :refer [run-jetty]]
            [service.wx :as wx]
            [service.user :as user]
            [clojure.tools.logging :as log]))

(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Newbiee"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]

      (GET "/plus" []
        :return {:result Long}
        :query-params [x :- Long, y :- Long]
        :summary "adds two numbers together"
        (ok {:result (+ x y)}))

      (GET "/hello" []
        :summary "echo Hello world"
        (ok "Hello world")))

    (context "/wx" []
      :tags ["wx"]

      (GET "/public_wx_accesstoken" []
        :summary "得到微信公众测试号的accesstoken (好像没啥用？)"
        ;(def actk (wx/get-weixin-accesstoken))
        (ok (wx/get-weixin-accesstoken)))

      (GET "/get_auth_link" []

        :summary "微信重定向授权"
        :query-params [{x :- String "http://zgty4v.natappfree.cc"}]
        (log/info x)
        ;(println x)
        ;(println (wx/weixin-oauth-link x))
        (def auth (wx/weixin-oauth-link x))
        (permanent-redirect auth)
        ;(ok auth)
        )

      (GET "/get-code" []
        :summary "得到用户微信code, 之后再拿用户的access token"
        :query-params [code :- String, state :- String]
        (ok (wx/get-weixin-accesstoken-by-code code)))

      (GET "/get-info" []
        :summary "得到用户信息"
        :query-params [access_token :- String openid :- String]
        (ok (wx/get-user-info access_token openid))))

    (context "/user" []
      :tags ["user"]

      (POST "/register" []
        :summary "注册用户, 使用用户的微信openid"
        :body [body {:openid s/Str
                     (s/optional-key :name) s/Str
                     (s/optional-key :sex) s/Str
                     (s/optional-key :country) s/Str
                     (s/optional-key :province) s/Str}]
        (ok (user/register body)))

      (POST "/login" []
        :summary "登陆"
        :body [body {:openid s/Str}]
        (let [response (user/login body)]
          (if (response :isSuccess) (assoc-in (ok response) [:session :identity] body)
                                    (ok response))))

      (POST "/logout" []
        :summary "注销"
        :body [body {:openid s/Str}]
        (assoc-in (ok) [:session :identity] nil))

      (POST "/order" []
        :summary "下订单"
        :body [body {:openid s/Str
                     :pid s/Str}]
        (ok (user/order body))))))

(def reload-app
  (wrap-reload #'app))

(defn -main
  [& _]
  (run-jetty reload-app {:port 8080 :join? false}))