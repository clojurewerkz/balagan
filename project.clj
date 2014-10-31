(defproject clojurewerkz/balagan "1.1.0-SNAPSHOT"
  :description "A tiny library for data structure transformation and querying"
  :url "http://github.com/clojurewerkz/balagan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]

  :source-paths ["src/cljx" "target/classes"]
  :test-paths   ["target/test-classes"]


  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha3"]]}
             :master {:dependencies [[org.clojure/clojure "1.7.0-master-SNAPSHOT"]]}
             :dev {:dependencies [[org.clojure/clojurescript "0.0-2138"]]
                   :plugins [[codox "0.8.10"]
                             [com.keminglabs/cljx "0.3.2"]
                             [lein-cljsbuild "1.0.2"]
                             [com.cemerick/clojurescript.test "0.2.1"]]
                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :clj}
                                   {:source-paths ["src/cljx"]
                                    :output-path "target/classes"
                                    :rules :cljs}
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :clj}
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test-classes"
                                    :rules :cljs}]}
                   :cljsbuild {:test-commands {"phantom" ["phantomjs" :runner "target/testable.js"]}
                               :builds [{:source-paths ["target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/testable.js"
                                                    :libs [""]
                                                    :source-map "target/testable.js.map"
                                                    :optimizations :advanced}}]}
                   :codox {:sources ["src/cljx"]
                           :output-dir "doc/api"}}}
  :aliases  {"all" ["with-profile" "+dev:+1.4:+1.5:+master:+1.7"]}
  :jar-exclusions [#"\.cljx"]
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  )
