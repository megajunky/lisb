(ns lisb.examples.crowded-chessboard
  (:require [clojure.set :refer [union]])
  (:require [lisb.core :refer [eval state-space]])
  (:require [lisb.representation :refer :all])
  (:require [lisb.translation :refer [to-ast]]))


(defn attack-horizontal 
  "only considers fields to the right (symmetry optimisation)"
  [n i j]
  (map #(vector % j) (range (inc i) (inc n))) )

(defn attack-vertical 
  "only considers fields above (symmetry optimisation)"
  [n i j]
  (map (partial vector i) (range (inc j) (inc n))))

(defn attack-diag1
  "only considers fields to the right (symmetry optimisation)"
  [n i j]
  (for [k (range 1 #_(- n) (inc n))
        :let [a (+ i k)
              b (+ j k)]
        :when (and (<= 1 a n) (<= 1 b n) (not= n 0))]
    [a b]))

(defn attack-diag2 
  "only considers fields to the right (symmetry optimisation)"
  [n i j]
  (for [k (range 1 #_(- n) (inc n))
        :let [a (+ i k)
              b (- j k)]
        :when (and (<= 1 a n) (<= 1 b n) (not= n 0))]
    [a b]))

(defn attack-diag
  "only considers fields to the right (symmetry optimisation)"
  [n i j]
  (concat (attack-diag1 n i j)
          (attack-diag2 n i j)))

(defn attack-knight
  "only considers fields to the right (symmetry optimisation)"
  [n i j]
  (for [[x y] [[1 2] #_[-1 2] [1 -2] #_[-1 -2]
               [2 1] #_[-2 1] [2 -1] #_[-2 -1]]
        :let [a (+ x i)
              b (+ y j)]
        :when (and (<= 1 a n) (<= 1 b n))]
    [a b]))

(attack-horizontal 8 4 4)
(attack-vertical 8 4 4)
(attack-diag1 8 3 4)
(attack-diag2 8 3 4)
(attack-diag 8 3 4)
(attack-knight 8 3 4)

(defn attack-queen [size i j]
  (concat (attack-horizontal size i j)
          (attack-vertical size i j)
          (attack-diag size i j)))

(defn attack-rook [size i j]
  (concat (attack-horizontal size i j)
          (attack-vertical size i j)))

(def attack-bishop attack-diag)

(defn attack [size figure attack-fn]
  (clojure.core/apply band (for [i (range 1 (inc size))
                                 j (range 1 (inc size))
                                 [a b] (attack-fn size i j)]
                             (b=> (b= figure (bapply (bapply :board i) j))
                                  (bnot= figure (bapply (bapply :board a) b))))))

(defn how-many [figure amount]
  (b= amount (bcount (blambda [:x :y]
                              (b= (bstr figure) (bapply (bapply :board :x) :y))
                              (btuple :x :y)))))

(defn crowded-chessboard
  "describes the crowded chessboard puzzle"
  ([size amount-knights ss]
   (let [width (binterval 1 :n)
         figures #{(bstr "queen")
                   (bstr "rook")
                   (bstr "bishop")
                   (bstr "knight")
                   (bstr "empty")}
         repr (b (and (= :n size)
                      (= :figures figures)
                      (= (count figures) 5)
                      (bmember :board (b--> width (--> width :figures)))
                      (how-many "queen" size)
                      ;(how-many "rook" size)
                      ;(how-many "bishop" (- (* 2 size) 2))
                      (how-many "knight" amount-knights)
                      ;(attack size (bstr "queen") attack-queen)
                      ;(attack size (bstr "rook") attack-rook)
                      ;(attack size (bstr "bishop") attack-bishop)
                      (attack size (bstr "knight") attack-knight)))
         result (eval ss (to-ast repr))]
     repr result))
  ([size amount-knights]
   (let [ss (state-space)]
     (crowded-chessboard size amount-knights ss))))

(clojure.repl/pst)

(def ss (state-space))
;; dis broken
;;(eval (to-ast (bmember :x (b--> #{1 2 3 4} #{1 2 3 4} #{1 2}))))
(eval (to-ast (band (bmember :x (b--> #{1 2 3 4} (b--> #{1 2 3 4} #{1 2})))
                    (b= :y (bapply (bapply :x 1) 2))
                    )))
(clojure.pprint/pprint (crowded-chessboard 4 3))