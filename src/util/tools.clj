(ns util.tools
  (:require [clojure.java.io :as cio]
            [clojure.edn :as edn])
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDateTime)))

(defn load-edn
  "load edn configuration file"
  [edn-filename]
  (with-open [in-edn ( -> edn-filename
                          cio/resource
                          cio/reader
                          (java.io.PushbackReader. ))]
    (edn/read in-edn)))

(defn now [] (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss" ) (LocalDateTime/now)))

(defn createTime [data]
  (assoc data :createTime (now)))

(defn updateTime [data]
  (assoc data :updateTime (now)))

(defn check-Empty? [coll]
  (contains? (set (map count coll)) 0))

(defn check-num [x]
  (every? (set "0123456789") x))

(defn not-all-numbers? [coll]
  (contains? (set (map check-num coll)) false))

; 多此一举了
;(defmulti time
;          "created time"
;          (fn [node data] (:tag node)))
;(defmethod time :createTime
;  [node data]
;  (assoc data :createTime (now)))
;
;(defmethod time :updateTime
;  [node data]
;  (assoc data :updateTime (now)))