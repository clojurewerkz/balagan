(ns com.ifesdjeen.balagan.core)

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn- path?
  [v]
  (and (sequential? v)
       (not (empty? v))
       (:path (meta v))))

(defn- p
  [v]
  (vary-meta v assoc :path true))

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

    Object
    (recurse-with-path [m path]
      (p path)))

(defn extract-paths
  "Extracts paths from the given sequence"
  [s]
  (-> s
      (recurse-with-path [])
      get-paths))

(def star? #(= :* %))

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
  [path pattern]
  (cond
   (= path pattern)    true
   (= (count path)
      (count pattern)) (every?
                        (fn [[a b]] (a b))
                        (partition 2 (interleave
                                      (resolve-pattern pattern)
                                      path)))
      :else               false))

(defn filter-matching-paths
  [paths pattern]
  (vec (filter #(path-matches? % pattern) paths)))

(defn matching-paths
  [m bodies]
  (let [all-paths (extract-paths m)
        paths     (apply hash-map
                         (mapcat identity
                                 (for [[selector transformation] (partition 2 (vec bodies))]
                                   (vec (interleave (filter-matching-paths all-paths selector) (repeat 3 transformation))))))]
    paths))

(defmacro transform
  [m & bodies]
  (let [matched-parts (matching-paths m bodies)]
    `(reduce (fn [acc# [path# transformation#]]
               (assoc-in acc# path#
                         (cond
                          (fn? transformation#) (transformation# (get-in acc# path#))
                          :else                 transformation#))
               ) ~m ~matched-parts)))
