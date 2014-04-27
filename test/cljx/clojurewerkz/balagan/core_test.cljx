(ns clojurewerkz.balagan.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest testing)])
  (:require [clojure.set :as cs]
            [clojurewerkz.balagan.core :as b]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]))

(defn vec-contains?
  "Wether the vec contains certain val"
  [vec val]
  (not (nil? (some #(= val %) vec))))

(defn has-all-paths?
  [extracted paths]
  (do
    (is (= (count extracted)
           (count paths))
        (str "Unmatched extracted paths: "
             (cs/difference (set extracted) (set paths))))
    (doseq [path paths]
      (is (vec-contains? extracted path)
          (str "Vector `" (vec extracted) "` doesn't contain path `" path "`")))))

(deftest extract-paths-test
  (testing "Extract paths from a vector"
    (has-all-paths? (b/extract-paths [1 2 3])
                    [[] [0] [1] [2]]))

  (testing "Extract paths from a simple map"
    (has-all-paths? (b/extract-paths {:a {:b {:c :d}}})
                    [[]
                     [:a]
                     [:a :b]
                     [:a :b :c]]))

  (testing "Extract paths from a map that has vectors"
    (has-all-paths? (b/extract-paths {:a :b :c {:d :e :f {:g '(1 2 3)}}})
                    [[]
                     [:a]
                     [:c]
                     [:c :d]
                     [:c :f]
                     [:c :f :g]
                     [:c :f :g 0]
                     [:c :f :g 1]
                     [:c :f :g 2]])))

(deftest path-matches?-test
  (is (b/path-matches? [0 1 2] [0 1 2]))
  (is (b/path-matches? [0 :* 2] [0 1 2]))
  (is (b/path-matches? [:c :f :g :*] [:c :f :g 0]))
  (is (b/path-matches? [0 odd? 2] [0 1 2])))

(deftest transform-test
  (let [res (b/update
             {:a :b :c {:d :e :f {:g [1 2 3]}}}
             [:c :f :g :*] inc)]
    (is (= {:a :b, :c {:f {:g [2 3 4]}, :d :e}})
        res)))

(deftest add-field-test
  (let [res (b/update
             {:a :b}
             [] (b/do->
                 (b/add-field :c :d)
                 (b/add-field :e :f)))]
    (is (= {:e :f :c :d :a :b}
           res))))

(deftest remove-field-test
  (let [res (b/update
             {:e :f :c :d :a :b}
             [] (b/do->
                 (b/remove-field :e)
                 (b/remove-field :c)))]
    (is (= {:a :b}
           res))))

(deftest fn-test
  (let [m {:a :1}
        res (b/update m [] #(assoc % :b 2))]
    (is (= 2 (:b res)))))

(deftest add-node-test
  (let [m {:a 1}
        res (b/update m
                      (b/mk-path [:inc-a]) #(inc (:a %)))]
    (is (= 2
           (:inc-a res))))

  (let [m {:a :1}
        res (b/update m
                      (b/mk-path [:b]) 2)]
    (is (= 2
           (:b res)))))


(deftest select-test
  (b/select {:a :b :c {:d :e :f {:g [1 2 3]}}}
          [:a] #(do
                  (is (= :b %1))
                  (is (= [:a] %2))))


  (b/select {:a {:b {:c 1} :d {:c 2}}}
          [:a :* :c] (fn [val path]
                       (if (= path [:a :b :c])
                         (is (= val 1))
                         (is (= val 2)))))

  (b/select {:a {1 {:c 1} 2 {:c 2} 3 {:c 3}}}
          [:a odd? :c] (fn [val path]
                         (if (= path [:a 1 :c])
                           (is (= val 1))
                           (is (= val 3)))))

  (b/select {:a [{:c 1} {:c 2} {:c 3}]}
          [:a even? :c] (fn [val path]
                          (if (= path [:a 0 :c])
                            (is (= val 1))
                            (is (= val 3))))))


(deftest transform-lazy-test
  (is (= {:a {:b [3 4 5]}}
         (b/update {:a {:b (map inc [1 2 3])}}
                   [:a :b :*] inc))))


(deftest transform-vector-test
  (is (= [2 3 4]
         (b/update [1 2 3]
                   [:*] inc)))
  (is (= [3 4 5]
         (b/update (map inc [1 2 3])
                   [:*] inc)))
  (is (= [{:a 2} {:a 3} {:a 4}]
         (b/update [{:a 1} {:a 2} {:a 3}]
                   [:* :a] inc))))

(deftest transform-dynamic-paths-test
  (let [res (b/update
             {:a {:b {}}}
             (b/mk-path [:a :b :c]) (constantly {:d 1})
             [:a :b :c :d] inc)]
    (is (= {:a {:b {:c {:d 2}}}}
           res))))


(deftest transform-empty-list
  (let [res (b/update [{:a {:b '()}}
                       {:a {:b (map (constantly 1) [1])}}
                       {:a 1}]
                      [:* :a :b :*] inc)]
    (is (= [{:a {:b '()}} {:a {:b [2]}} {:a 1}]
           res))))
