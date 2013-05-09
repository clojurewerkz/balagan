(ns com.ifesdjeen.balagan.core-test
  (:require [clojure.set :as cs])
  (:use clojure.test
        com.ifesdjeen.balagan.core))

(defn vec-contains?
  "Wether the vec contains certain val"
  [vec val]
  (not (nil? (some #(= val %) vec))))

(defmacro has-all-paths?
  [extracted paths]
  `(do
     (is (= (count ~extracted)
            (count ~paths))
         (format "Unmatched extracted paths: %s"
          (cs/difference (set ~extracted) (set ~paths))))
     (doseq [path# ~paths]
       (is (vec-contains? ~extracted path#)
           (format "Vector `%s` doesn't contain path `%s`" (vec ~extracted) path#)))))

(deftest extract-paths-test
  (testing "Extract paths from a vector"
    (has-all-paths? (extract-paths [1 2 3])
                    [[] [0] [1] [2]]))

  (testing "Extract paths from a simple map"
    (has-all-paths? (extract-paths {:a {:b {:c :d}}})
                    [[]
                     [:a]
                     [:a :b]
                     [:a :b :c]]))

  (testing "Extract paths from a map that has vectors"
    (has-all-paths? (extract-paths {:a :b :c {:d :e :f {:g '(1 2 3)}}})
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
  (is (path-matches? [0 1 2] [0 1 2]))
  (is (path-matches? [0 1 2] [0 :* 2]))
  (is (path-matches? [:c :f :g 0] [:c :f :g :*]))
  (is (path-matches? [0 1 2] [0 odd? 2])))


(deftest transform-test
  (is (= {:a :b, :c {:f {:g [2 3 4]}, :d :e}})
      (transform
       {:a :b :c {:d :e :f {:g [1 2 3]}}}
       [:c :f :g :*] inc)))

(deftest add-field-test
  (is (= {:e :f :c :d :a :b}
         (transform
          {:a :b}
          [] (do->
              (add-field :c :d)
              (add-field :e :f))))))

(deftest remove-field-test
  (is (= {:a :b}
         (transform
          {:e :f :c :d :a :b}
          [] (do->
              (remove-field :e)
              (remove-field :c))))))

(deftest fn-test
  (let [m {:a :1}]
    (is (= 2
           (:b (transform m [] #(assoc % :b 2)))))))

(deftest add-node-test
  (let [m {:a 1}]
    (is (= 2
           (:inc-a
            (transform m
                       (new-path [:inc-a]) #(inc (:a %)))))))

  (let [m {:a :1}]
    (is (= 2
           (:b
            (transform m
                       (new-path [:b]) 2))))))
