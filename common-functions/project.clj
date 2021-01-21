(defproject common-functions "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [slingshot "0.12.2"]
                 [clj-http "3.10.3"]
                 [clj-time "0.15.2"]
                 [buddy/buddy-sign "3.3.0"]
                 [org.clojure/data.json "1.0.0"]
                 [org.postgresql/postgresql "42.2.18.jre7"]]
  :repl-options {:init-ns common-functions.core})
