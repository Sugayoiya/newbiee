(ns newbiee.core-test
  (:require [clojure.test :refer :all]
            [newbiee.handler :refer :all]
            [ring.mock.request :refer :all]
            [cheshire.core :as cs]
            [mongo.db :as db]))

(defn db_init
  [test-fn]
  (db/remove-coll "users")
  (test-fn))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 0))))

(deftest app-test
  ;(testing "not found"
  ;  (let [response (app (request :get "/api/invalid"))]
  ;    (is (= 404 response))))

  (testing "hello"
    (let [response (app (request :get "/api/hello"))]
      (is (= "Hello world" (:body response)))
      (is (= 200 (:status response))))

    (let [response (app (-> (request :get "/api/plus")
                            (query-string {:x 5 :y 6})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= body {:result 11}))
      (is (= 200 (:status response)))))

  (testing "wx"
    (let [response (app (request :get "/wx/public_wx_accesstoken"))]
      (is (= 200 (:status response))))

    (let [response (app (-> (request :get "/wx/get-code")
                            (query-string {:code "code" :state "state"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (select-keys body [:isSuccess :errcode :result]) {:isSuccess false, :errcode 40029, :result {}} ))
      (is (= 200 (:status response))))

    (let [response (app (-> (request :get "/wx/get-info")
                            (query-string {:access_token "1" :openid "2"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (= (select-keys body [:isSuccess :errcode :result]) {:isSuccess false, :errcode 40001, :result {}}))))

  (testing "user register"
    (let [response (app (-> (request :post "/user/register")
                            (json-body {:openid "test" :sex "1"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= (select-keys body [:isSuccess :errcode :errmsg :result])  {:isSuccess true,
                                                                        :errcode 0,
                                                                        :errmsg "",
                                                                        :result {:openid "test",
                                                                                 :sex "1"}}))))

  (testing "user already registered"
    (let [response (app (-> (request :post "/user/register")
                            (json-body {:openid "test" :sex "1"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= (select-keys body [:isSuccess :errcode :errmsg :result]) {:isSuccess false,
                                                                       :errcode "50010",
                                                                       :errmsg "user is already registered, please login",
                                                                       :result {}}))))

  (testing "user login"
    (let [response (app (-> (request :post "/user/login")
                            (json-body {:openid "test"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= (:session response) {:identity {:openid "test"}}))
      (is (= (select-keys body [:isSuccess :errcode :errmsg]) {:isSuccess true, :errcode 0, :errmsg ""}))
      ))

  (testing "unregistered user login"
    (let [response (app (-> (request :post "/user/login")
                            (json-body {:openid "test1"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= (select-keys body [:isSuccess :errcode :errmsg :result]) {:isSuccess false,
                                                               :errcode "40002",
                                                               :errmsg "user \"test1\" is not registered!",
                                                               :result {}}))))

  (testing "user make a order"
    (let [response (app (-> (request :post "/user/order")
                            (json-body {:openid "test"
                                        :pid "1"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= body {:isSuccess true, :errcode 0, :errmsg "", :result "order created"}))))

  (testing "unregistered user make a order"
    (let [response (app (-> (request :post "/user/order")
                            (json-body {:openid "test1"
                                        :pid "1"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= body {:isSuccess false, :errcode 40003, :errmsg "invalid openid or pid", :result {}}))))

  (testing "we don't have that product"
    (let [response (app (-> (request :post "/user/order")
                            (json-body {:openid "test"
                                        :pid "4"})))
          body (cs/parse-string (slurp (:body response)) true)]
      (is (= (:status response) 200))
      (is (= body {:isSuccess false, :errcode 40003, :errmsg "invalid openid or pid", :result {}}))))

  )

(use-fixtures :once db_init)
(run-tests)