(ns service.user
  (:require [mongo.db :as db]
            [clojure.tools.logging :as log]
            [util.res :as res]
            [cheshire.core :as cs])
  )

(defn register
  [userinfo]
  (let [{:keys [openid]} userinfo
        user (db/find-user openid)]
    (cond (nil? openid) (do (log/error "openid is empty")
                            (res/errorResponse "50001" "no openid provided"))
          (some? user) (do (log/error "user is already registered")
                           (res/errorResponse "50010" "user is already registered, please login"))
          :else (do (db/add-user userinfo)
                    (log/info (format "user registered, openid is \" %s \"" openid))
                    (res/succResponse userinfo)))))

(defn login
  [openid]
  (let [{:keys [openid]} openid
        user (db/find-user openid)]
    (cond (nil? user) (do (log/error (format "openid \"%s\" is not registered" openid))
                          (res/errorResponse "40002" (format "user \"%s\" is not registered!" openid)))
          (some? user) (do (log/info (format "user (openid \"%s\") has logged in" openid))
                           ;(println (.get (.toMap user) "openid"))
                           (res/succResponse  (str "user [" (.get (.toMap user) "openid") "] has logged in"))))))

(defn order
  [order-info]
  (let [{:keys [openid pid]} order-info
        user (db/find-user openid)
        product (db/find-product pid)
        order? (and user product)]
    (cond (not order?) (do (log/error "invalid openid or pid" openid pid)
                           (res/errorResponse 40003 "invalid openid or pid"))
          (nil? (db/add-order openid pid)) (do (log/error "error creating order")
                                               (res/errorResponse "40003" "order creation failure"))
          :else (do (log/info "new order created, openid: \"" openid "\", pid: \"" pid "\"")
                    (res/succResponse "order created")))))