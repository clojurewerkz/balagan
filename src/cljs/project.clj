(defproject clojurewerkz/balagan-cljs "0.4.0-SNAPSHOT"
  :description "A tiny library for data structure transformation and querying"
  :url "http://github.com/clojurewerkz/balagan"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2197"]
                 [com.cemerick/clojurescript.test "0.3.0"]]
  :source-paths  ["src" "test"]
  :jvm-opts ["-Xmx2048m"]
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.0"]]

  :cljsbuild
  {:builds
   {:dev  {:source-paths ["src"]
           :compiler {:output-to "target/main.js"
                      :output-dir "target"
                      :source-map "target/main.js.map"
                      :optimizations :whitespace
                      :pretty-print true}}
    :test {:source-paths ["src" "test"]
           :incremental? true
           :compiler {:output-to "target-test/unit-test.js"
                      :output-dir "target-test"
                      :source-map "target-test/unit-test.js.map"
                      :optimizations :whitespace
                      :pretty-print true}}}
   :test-commands {"unit-tests"
                   ["phantomjs" :runner "target-test/unit-test.js"]}}
  )
