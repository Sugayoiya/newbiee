(ns util.res)

(defn errorResponse
  ([errcode errmsg]
   {:isSuccess false
    :errcode errcode
    :errmsg errmsg
    :result {}})
  ([response]
   (errorResponse (:errcode response) (:errmsg response)))
  )

(defn succResponse
  [result]
  {:isSuccess true
   :errcode 0
   :errmsg ""
   :result result})