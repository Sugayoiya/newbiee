(ns mongo.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [util.tools :as tool])
  (:import  [com.mongodb MongoOptions ServerAddress]
            (org.bson.types ObjectId)))

(def conf (util.tools/load-edn "mongodb.edn"))

(def cu (:coll_users conf))
;db.createCollection("users")
;db.users.createIndex({"openid":1},{unique:true})

(def co (:coll_orders conf))
;db.createCollection("orders")
;db.orders.createIndex({"orderid":1},{unique:true})

(def cp (:coll_products conf))
;db.createCollection("products")
;db.products.createIndex({"pid":1},{unique:true})

(def coll-map {"orders" co
               "users" cu
               "products" cp
               })

(def conn (let [^MongoOptions opts (mg/mongo-options {:option conf})
                ^ServerAddress sa (mg/server-address (:host conf) (:port conf))]
            (mg/connect sa opts)))

(def db (mg/get-db conn (:db conf)))

(defn add-user
  "add user info in users collection"
  [userinfo]
  (mc/insert db cu (-> userinfo
                       tool/createTime
                       tool/updateTime)))

(defn add-order
  "add order info into order collection"
  [^String openid ^String pid]
  ;{:pre [(not (tool/check-Empty? [openid pid])) (not (tool/not-all-numbers? [openid pid]))]}
  (mc/insert db co (-> {:_id (ObjectId.)
                        :openid openid
                        :pid pid}
                       tool/createTime
                       tool/updateTime)))

(defn add-product
  "add product inffo into products collection"
  [^String pid ^String pname ^String pdes]
  {:pre [(not (tool/check-Empty? [pid])) (not (tool/not-all-numbers? [pid]))]}
  (mc/insert db cp (-> {:pid pid
                        :pname pname
                        :pdes pdes}
                       tool/createTime
                       tool/updateTime)))

(defn find-user
  [openid]
  (mc/find-one db cu {:openid openid}))

(defn find-user-as-map
  [openid]
  (mc/find-one-as-map db cu {:openid openid}))

(defn find-order
  [orderid]
  (mc/find-one db co {:orderid orderid}))

(defn find-order-as-map
  [orderid]
  (mc/find-one-as-map db co {:orderid orderid}))

(defn find-product
  [pid]
  (mc/find-one db cp {:pid pid}))

(defn find-product-as-map
  [pid]
  (mc/find-one-as-map db cp {:pid pid}))

(defn find-maps
  [coll]
  (mc/find-maps db coll))

(defn remove-coll
  "remove documents in { users, orders, products } collections"
  [^String coll]
  {:pred (contains? #{"users", "orders", "products"} coll)}
  (mc/remove db ( {"orders" co
                   "users" cu
                   "products" cp
                   } coll)))

;(defmulti remove-collection-document
;          "remove documents in {users, orders, products} collection"
;          ((fn [coll] (coll-map coll))))

