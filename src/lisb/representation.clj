(ns lisb.representation
  (require [clojure.math.combinatorics :refer [combinations]]))


(defn node [tag & children]
  {:tag tag
   :children children})


(defn chain [tag tuples]
  (reduce (partial node :and) (map (partial apply node tag) tuples)))

(defn chain-arity-two [tag nodes]
  (chain tag (partition 2 1 nodes)))

(defn combine-and-chain [tag nodes]
  (chain tag (combinations nodes 2)))

(defn interleave-arity-two [tag nodes]
  (reduce (partial node tag) nodes))



(defn b< [& args]
  (chain-arity-two :less args))

(defn b+ [& args]
  (interleave-arity-two :plus args))

(defn b- [a & r]
  (if (seq r)
    (interleave-arity-two :minus (conj r a))
    (node :unaryminus a)))

(defn band [& args]
  (interleave-arity-two :and args))

(defn b= [& args]
  (chain-arity-two :equals args))

(defn b<=> [& args]
  (chain-arity-two :equivalence args))

(defn bor [& args]
  (interleave-arity-two :or args))

(defn bnot [a]
  (node :not a))

(defn bnot= [& args]
  (combine-and-chain :not-equals args))

(defn bpred->bool [a]
  (node :to-bool a))

(defn bset [v p]
  (node :comp-set v p))

(defn bpow [s]
  (node :power-set s))

(defn bpow1 [s]
  (node :power1-set s))

(defn bfin [s]
  (node :finite-subset s))

(defn bfin1 [s]
  (node :finite1-subset s))

(defn bcount [s]
  (node :card s))

(defn bx [& args]
  (interleave-arity-two :cartesian-product args))

(defn bunion [& args]
  (interleave-arity-two :set-union args))

(defn bintersect [& args]
  (interleave-arity-two :set-intersection args))

(defn bset- [& args]
  (interleave-arity-two :set-difference args))

(defn bmember [e & sets]
  (chain :member (map (fn [s] [e s]) sets)))

(defn bmembers [s & elements]
  (chain :member (map (fn [e] [e s]) elements)))

(defn bsubset [& args]
  (chain-arity-two :subset args))

(defn bsuperset [& args]
  (apply bsubset (reverse args)))

; TODO: - implication (is it left- or right-associative?)
;       - exists
;       - forall
;       - bool-set
