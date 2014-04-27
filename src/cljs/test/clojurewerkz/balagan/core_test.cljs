(ns clojurewerkz.balagan.core-test
  (:require-macros
   [cemerick.cljs.test :refer (is deftest with-test run-tests testing)]
   [clojurewerkz.balagan.core :refer [update select add-field remove-field]])
  (:require [clojure.set :as cs]
            [cemerick.cljs.test :as t]
            [clojurewerkz.balagan.core :as balagan :refer (do-> make-new-path)]))


(deftest update-test
  (let [res (update
             {:a :b :c {:d :e :f {:g [1 2 3]}}}
             [:c :f :g :*] inc)]
    (is (= [2 3 4] (get-in res [:c :f :g]) ))))

(deftest add-field-test
  (let [res (update
             {:a :b}
             [] (do->
                 (add-field :c :d)
                 (add-field :e :f)))]
    (is (= {:e :f :c :d :a :b}
           res))))

(deftest remove-field-test
  (let [res (update
             {:e :f :c :d :a :b}
             [] (do->
                 (remove-field :e)
                 (remove-field :c)))]
    (is (= {:a :b}
           res))))

(deftest fn-test
  (let [m {:a :1}
        res (update m [] #(assoc % :b 2))]
    (is (= 2 (:b res)))))

(deftest add-node-test
  (let [m {:a 1}
        res (update m
                       (make-new-path [:inc-a]) #(inc (:a %)))]
    (is (= 2
           (:inc-a res))))

  (let [m {:a :1}
        res (update m
                       (make-new-path [:b]) 2)]
    (is (= 2
           (:b res)))))

(deftest select-test
  (select {:a :b :c {:d :e :f {:g [1 2 3]}}}
          [:a] #(do
                  (is (= :b %1))
                  (is (= [:a] %2))))


  (select {:a {:b {:c 1} :d {:c 2}}}
          [:a :* :c] (fn [val path]
                       (if (= path [:a :b :c])
                         (is (= val 1))
                         (is (= val 2)))))

  (select {:a {1 {:c 1} 2 {:c 2} 3 {:c 3}}}
          [:a odd? :c] (fn [val path]
                         (if (= path [:a 1 :c])
                           (is (= val 1))
                           (is (= val 3)))))

  (select {:a [{:c 1} {:c 2} {:c 3}]}
          [:a even? :c] (fn [val path]
                          (if (= path [:a 0 :c])
                            (is (= val 1))
                            (is (= val 3))))))


(deftest transform-lazy-test
  (is (= {:a {:b [3 4 5]}}
         (update {:a {:b (map inc [1 2 3])}}
                    [:a :b :*] inc))))


(deftest transform-vector-test
  (is (= [2 3 4]
         (update [1 2 3]
                    [:*] inc)))
  (is (= [3 4 5]
         (update (map inc [1 2 3])
                    [:*] inc)))
  (is (= [{:a 2} {:a 3} {:a 4}]
         (update [{:a 1} {:a 2} {:a 3}]
                    [:* :a] inc))))

(deftest transform-dynamic-paths-test
  (let [res (update
             {:a {:b {}}}
             (make-new-path [:a :b :c]) (constantly {:d 1})
             [:a :b :c :d] inc)]
    (is (= {:a {:b {:c {:d 2}}}}
           res))))

(deftest transform-empty-list
  (let [res (update [{:a {:b '()}}
                        {:a {:b (map (constantly 1) [1])}}
                        {:a 1}]
                       [:* :a :b :*] inc)]
    (is (= [{:a {:b '()}} {:a {:b [2]}} {:a 1}]
           res))))
