(defproject warehouse-service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [yogthos/config "1.1.7"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.18.jre7"]
                 [honeysql "1.0.444"]
                 [clj-http "3.10.3"]
                 [org.clojure/data.json "1.0.0"]
                 [common-functions "0.1.0-SNAPSHOT"]
                 [compojure "1.6.1"]
                 [com.novemberain/langohr "5.2.0"]
                 [com.fzakaria/slf4j-timbre "0.3.20"]
                 [slingshot "0.12.2"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-json "0.4.0"]]
  :main ^:skip-aot warehouse-service.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
