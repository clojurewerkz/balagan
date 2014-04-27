(ns clojurewerkz.balagan.core
  (:require clojure.walk))

(defn unlazify-seqs
  [m]
  (let [f (fn [[k v]] (if (seq? v) [k (vec v)] [k v]))]
    (clojure.walk/postwalk (fn [x]
                             (cond
                              (map? x) (into {} (map f x))
                              (sequential? x) (into [] (map identity x))
                              :else x)) m)))


(def star-node  :*)
(def star?      #(= star-node %))
(def root-node? #(empty? %))
(def new-path-node :__new-path__)

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn- path?
  [v]
  (and (sequential? v)
       (:path (meta v))))

(defn is-new-path?
  [v]
  (and (path? v)
       (new-path-node (meta v))))

(defn- p
  [v]
  (vary-meta v assoc :path true))

(defn make-new-path
  [v]
  (p (vary-meta v assoc new-path-node true)))

(defn- get-paths
  [x]
  (filter path?
          (rest (tree-seq sequential? seq x))))

(defn recurse-with-path
  [m path]
  (cond
   (map? m) (conj
             (for [[k v] m]
               (recurse-with-path v (conj path k)))
             (p path))
   (or (seq? m)
       (list? m)) (recurse-with-path (mapv identity m) path)
   (or
       (vector? m)
       (set? m)) (conj
                   (for [[k v] (indexed m)]
                     (recurse-with-path v (p (conj path k))))
                   (p path))
   :else    (p path)))

(defn extract-paths
  "Extracts paths from the given sequence"
  [s]
  (-> s
      (recurse-with-path [])
      get-paths))

(defn resolve-pattern
  "TODO: DOCSTRING"
  [pattern]
  (into []
        (for [part pattern]
          (cond
           (star? part) (constantly true)
           (fn? part)   part
           :else        #(= part %)))))

(defn path-matches?
  [pattern path]
  (cond
   (= path pattern)    true
   (= (count path)
      (count pattern)) (every?
                        (fn [[a b]] (a b))
                        (partition 2 (interleave
                                      (resolve-pattern pattern)
                                      path)))
      :else             false))

(defn filter-matching-paths
  [paths pattern]
  (let [matched-parts (filter (partial path-matches? pattern) paths)]
    (vec
     (if (is-new-path? pattern)
       (conj matched-parts pattern)
       matched-parts))))

(defn expand-path
  [m [selector transformation]]
  (let [all-paths (extract-paths m)
        paths (filter-matching-paths all-paths selector)]
    (interleave paths (repeat (count paths) transformation))))

(defn matching-paths
  [m bodies]
  (let [all-paths (extract-paths m)
        expand-fn (partial expand-path m)]
    (->> (partition 2 (vec bodies))
         expand-fn
         (mapcat identity)
         (apply hash-map))))

(defn do->
  "Chains (composes) several transformations. Applies functions from left to right."
  [& fns]
  #(reduce (fn [acc f] (f acc)) % fns))
