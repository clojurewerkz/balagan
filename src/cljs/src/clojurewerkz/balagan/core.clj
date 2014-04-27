(ns clojurewerkz.balagan.core
  (:require clojure.walk))

(defmacro update
  [m & bodies]
  (let [bodies-v (vec bodies)]
    `(loop [acc#    (clojurewerkz.balagan.core/unlazify-seqs ~m)
            bodies# (partition 2 ~bodies-v)]
       (if (not (empty? bodies#))
         (recur
          (reduce (fn [acc# [path# transformation#]]
                    (cond
                     (clojurewerkz.balagan.core/root-node? path#) (transformation# acc#)
                     (clojurewerkz.balagan.core/is-new-path? path#)  (assoc-in acc# path#
                                                                            (cond
                                                                             (fn? transformation#) (transformation# acc#)
                                                                             :else                 transformation#))
                     :else              (assoc-in acc# path#
                                                  (cond
                                                   (fn? transformation#) (transformation# (get-in acc# path#))
                                                   :else                 transformation#))))
                  acc# (partition 2 (clojurewerkz.balagan.core/expand-path acc# (first bodies#))))
          (rest bodies#))
         acc#))))

(defmacro select
  [m & bodies]
  (let [bodies-v (vec bodies)]
    `(reduce (fn [acc# [path# funk#]]
               (cond
                (clojurewerkz.balagan.core/root-node? path#) (funk# acc# path#)
                :else                                        (funk# (get-in acc# path#) path#))
               acc#)
             ~m (clojurewerkz.balagan.core/matching-paths ~m ~bodies-v))))

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
