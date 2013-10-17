(ns clojurewerkz.balagan.core
  (:require clojure.walk))

(defn unlazify-seqs
  [m]
  (let [f (fn [[k v]]
            (if (instance? clojure.lang.LazySeq v)
                       [k (vec v)]
                       [k v]))]
    (clojure.walk/postwalk (fn [x]
                             (cond
                              (map? x) (into {} (map f x))
                              (sequential? x) (into [] (map identity x))
                              :else x)) m)))

(def star-node  :*)
(def star?      #(= star-node %))
(def root-node? #(empty? %))

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

(defn new-path?
  [v]
  (and (path? v)
       (:new-path (meta v))))

(defn- p
  [v]
  (vary-meta v assoc :path true))

(defn new-path
  [v]
  (p (vary-meta v assoc :new-path true)))

(defn- get-paths
  [x]
  (filter path?
          (rest (tree-seq sequential? seq x))))

(defprotocol RecurseWithPath
  (recurse-with-path [v path]))

(extend-protocol RecurseWithPath
  clojure.lang.IPersistentMap
  (recurse-with-path [m path]
    (conj
     (for [[k v] m]
       (recurse-with-path v (conj path k)))
     (p path)))

  clojure.lang.IPersistentCollection
  (recurse-with-path [m path]
    (conj
     (for [[k v] (indexed m)]
       (recurse-with-path v (p (conj path k))))
     (p path)))

  clojure.lang.LazySeq
  (recurse-with-path [m path]
    (recurse-with-path (vec m) path))

  Object
  (recurse-with-path [m path]
    (p path))

  nil
  (recurse-with-path [m path]
    nil))

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
     (if (new-path? pattern)
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

(defmacro transform
  [m & bodies]
  (let [bodies-v (vec bodies)]
    `(loop [acc#    (unlazify-seqs ~m)
            bodies# (partition 2 ~bodies-v)]
       (if (not (empty? bodies#))
         (recur
          (reduce (fn [acc# [path# transformation#]]
                    (cond
                     (root-node? path#) (transformation# acc#)
                     (new-path? path#)  (assoc-in acc# path#
                                                  (cond
                                                   (fn? transformation#) (transformation# acc#)
                                                   :else                 transformation#))
                     :else              (assoc-in acc# path#
                                                  (cond
                                                   (fn? transformation#) (transformation# (get-in acc# path#))
                                                   :else                 transformation#))))
                  acc# (partition 2 (expand-path acc# (first bodies#))))
          (rest bodies#))
         acc#))))

(defmacro select
  [m & bodies]
  (let [bodies-v (vec bodies)]
    `(reduce (fn [acc# [path# funk#]]
               (cond
                (root-node? path#) (funk# acc# path#)
                :else              (funk# (get-in acc# path#) path#))
               acc#)
             ~m (matching-paths ~m ~bodies-v))))
;;
;; Helpers
;;

(defmacro add-field
  "Adds field to the selected entry"
  [field & body]
  `(fn [m#]
     (assoc m# ~field ~@body)))

(defmacro remove-field
  "Removes field from selected entry"
  [field]
  `(fn [m#]
     (dissoc m# ~field)))

(defn do->
  "Chains (composes) several transformations. Applies functions from left to right."
  [& fns]
  #(reduce (fn [acc f] (f acc)) % fns))
