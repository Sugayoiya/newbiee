 (defproject newbiee "0.1.1"
   :description "jiliguala: newbiee project"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  [metosin/compojure-api "2.0.0-alpha30"]
                  [com.novemberain/monger "3.5.0"]
                  [clj-http "3.10.0"]
                  [org.clojure/tools.logging "0.5.0"]
                  [ring/ring-codec "1.1.2"]
                  [ring/ring-devel "1.8.0"]
                  [ring/ring-codec "1.1.2"]
                  [ring/ring-jetty-adapter "1.8.0"]
                  [ring/ring-defaults "0.3.2"]
                  [org.apache.logging.log4j/log4j-slf4j18-impl "2.13.0"]
                  [cheshire/cheshire "5.8.1"]
                  [ring/ring-mock "0.3.2"]
                  ]
   :ring {:handler newbiee.handler/reload-app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                   [ring/ring-mock "0.3.2"]]
                    :plugins [[lein-ring "0.12.5"]]
                    :resource-paths ["resources/dev"]}
              :prod {:resource-paths ["resources/prod"]}})
