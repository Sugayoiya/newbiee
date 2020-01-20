(ns service.wx
  (:require [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [ring.util.codec :as encoder]
            [util.res :as res]))

(def appid "wxa3ae4ca5005419bc")
(def secret "e709b6ceb90e46b3945641274a31c3bb")
(def grant_type "client_credential")
;(def openid "odWJQwh9l8Q3elcN6LV2my88labI")

(defn get-weixin-accesstoken-by-code
  "get weixin access token by code"
  [code]
  (try
    (let [response (client/get "https://api.weixin.qq.com/sns/oauth2/access_token"
                               {:query-params {:appid appid
                                               :secret secret
                                               :code code
                                               :grant_type "authorization_code"}
                                :as :json})
          weixin-accesstoken (:body response)]
      (if (:errcode weixin-accesstoken)
        (do
          (log/error "wx access token failure, errcode:" (:errcode weixin-accesstoken) " errmsg: " (:errmsg weixin-accesstoken))
          (res/errorResponse weixin-accesstoken))
        (do
          (log/info "wx access token success")
          (res/succResponse weixin-accesstoken))))
    (catch Exception e
      (do
        (log/error "get weixin access token (by code) error" (.getMessage e))
        (res/errorResponse 40404 (.getMessage e))))))

(defn get-weixin-accesstoken
  "get weixin access token (useless)"
  []
  (try
    (let [response (client/get "https://api.weixin.qq.com/cgi-bin/token"
                               {:query-params {:grant_type grant_type
                                               :appid appid
                                               :secret secret}
                                :as :json})
          weixin-accesstoken (:body response)]
      (if (:errcode weixin-accesstoken)
        (do
          (log/error "get access token failure" (:errcode weixin-accesstoken) " errmsg: " (:errmsg weixin-accesstoken))
          (res/errorResponse (:errcode weixin-accesstoken) (:errmsg weixin-accesstoken)))
        (do
          (log/info "weixin access token" weixin-accesstoken)
          (res/succResponse weixin-accesstoken))))
    (catch Exception e
      (log/error "weixin access token failure: " (.getMessage e))
      (res/errorResponse 50404 (.getMessage e)))))

;(defn get-weixin-info
;  "get yourself information"
;  [access-token]
;  (try
;    (let [response (client/get "https://api.weixin.qq.com/cgi-bin/user/info"
;                               {:query-params {:access_token access-token
;                                               :openid openid
;                                               :lang "zh_CN"}
;                                :as :json})
;          user-info (:body response)]
;      (log/info "user information" user-info))
;    (catch Exception e
;      (log/error e "get weixin info error"))))

(defn get-user-info
  "get user info by authorized token"
  [access-token openid]
  (try
    (let [response (client/get "https://api.weixin.qq.com/sns/userinfo"
                               {:query-params {:access_token access-token
                                               :openid openid
                                               :lang "zh_CN"}
                                :as :json})
          user-info (:body response)]
      ;(println response)
      (if (:errcode user-info)
        (do
          (log/error "get weixin user-info error, errcode: " (:errcode user-info) " errmsg: " (:errmsg user-info) )
          (res/errorResponse user-info))
        (do
          (log/info "get weixin user info success")
          (res/succResponse user-info)))
      )
    (catch Exception e
      (log/error "get user info error: " (.getMessage e))
      (res/errorResponse 50403 (.getMessage e)))))

(defn weixin-oauth-link
  "return the redirect url to user"
  [link]
  (let [auth-link-params {:appid appid
                          :redirect_url (encoder/url-encode (str link "/wx/get-code"))
                          :response_type "code"
                          :scope "snsapi_userinfo"
                          :state "123"}]
    (do
      (log/info (format "wx redirect url formed, start with : %s" link))
      (format "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=%s&scope=%s&state=%s#wechat_redirect"
                (:appid auth-link-params)
                (:redirect_url auth-link-params)
                (:response_type auth-link-params)
                (:scope auth-link-params)
                (:state auth-link-params)))))