(ns lisb.representation-test
  (:require [clojure.test :refer :all]
            [lisb.representation :refer :all]))


(deftest less-test
  (testing "less works with two arguments"
    (is (= {:tag :less
            :children [:a :b]}
           (b< :a :b))))
  (testing "less works with more than two arguments"
    (is (= {:tag :and
            :children [{:tag :less :children [:a :b]}
                       {:tag :less :children [:b :c]}]}
           (b< :a :b :c)))))

(deftest plus-test
  (testing "plus works with two arguments"
    (is (= {:tag :plus
            :children [:a :b]}
           (b+ :a :b))))
  (testing "plus works with more than two arguments"
    (is (= {:tag :plus
            :children [{:tag :plus :children [:a :b]}
                       :c]}
           (b+ :a :b :c)))))


(deftest and-test
  (testing "and works with two arguments"
    (is (= {:tag :and
            :children [:a :b]}
           (band :a :b))))
  (testing "and works with more than two arguments"
    (is (= {:tag :and
            :children [{:tag :and :children [:a :b]}
                       :c]}
           (band :a :b :c)))))

(deftest minus-test
  (testing "minus is special and must have a unary version"
    (is (= {:tag :unaryminus
            :children [:a]}
           (b- :a))))
  (testing "minus also works with two arguments"
    (is (= {:tag :minus
            :children [:a :b]}
           (b- :a :b))))
  (testing "minus also works as expected with more than two arguments"
    (is (= {:tag :minus
            :children [{:tag :minus :children [:a :b]}
                       :c]}
           (b- :a :b :c)))))

(deftest equals-test
  (testing "equals works with two arguments"
    (is (= {:tag :equals
            :children [:a :b]}
           (b= :a :b))))
  (testing "equals works with more than two arguments"
    (is (= {:tag :and
            :children [{:tag :equals :children [:a :b]}
                       {:tag :equals :children [:b :c]}]}
           (b= :a :b :c)))))


(deftest equivalence-test
  (testing "equals works with two arguments"
    (is (= {:tag :equivalence
            :children [:a :b]}
           (b<=> :a :b))))
  (testing "equals works with more than two arguments"
    (is (= {:tag :and
            :children [{:tag :equivalence :children [:a :b]}
                       {:tag :equivalence :children [:b :c]}]}
           (b<=> :a :b :c)))))



(deftest or-test
  (testing "or works with two arguments"
    (is (= {:tag :or
            :children [:a :b]}
           (bor :a :b))))
  (testing "or works with more than two arguments"
    (is (= {:tag :or
            :children [{:tag :or :children [:a :b]}
                       :c]}
           (bor :a :b :c)))))

(deftest not-test
  (testing "not is unary"
    (is (= {:tag :not
            :children [:a]}
           (bnot :a)))))

(deftest not-equals-test
  (testing "not-equals works with two arguments"
    (is (= {:tag :not-equals
            :children [:a :b]}
           (bnot= :a :b))))
  (testing "not-equals works with more than two arguments"
    (is (= {:tag :and,
            :children [{:tag :and, :children [{:tag :not-equals, :children [:a :b]}
                                              {:tag :not-equals, :children [:a :c]}]}
                       {:tag :not-equals :children [:b :c]}]}
           (bnot= :a :b :c))))
  (testing "not-equals generates data in a set-like semantics"
    (is (= {:tag :and
            :children [{:tag :not-equals :children [:a :a]}
                       {:tag :not-equals :children [:a :b]}]}))))

(deftest to-bool-test
  (testing "to-bool works as intended"
    (is (= {:tag :to-bool
            :children [:a]}
           (bpred->bool :a)))))

(deftest bset-test
  (testing "construction of a comprehension set"
    (is (= {:tag :comp-set
            :children [[:x]
                       {:tag :and :children [{:tag :less :children [1 :x]}
                                             {:tag :less :children [:x 5]}]}]}
           (bset [:x] (b< 1 :x 5))))))

(deftest powerset-test
  (testing "powerset representation"
    (is (= {:tag :power-set
            :children [#{1 2}]}
           (bpow #{1 2})))))

(deftest powerset1-test
  (testing "powerset1 representation"
    (is (= {:tag :power1-set
            :children [#{1 2}]}
           (bpow1 #{1 2})))))

(deftest finset-test
  (testing "finite subset representation"
    (is (= {:tag :finite-subset
            :children [#{1 2}]}
           (bfin #{1 2})))))

(deftest finset1-test
  (testing "finite subset representation"
    (is (= {:tag :finite1-subset
            :children [#{1 2}]}
           (bfin1 #{1 2})))))

(deftest card-test
  (testing "cardinality operator representation"
    (is (= {:tag :card
            :children [#{1 2}]}
           (bcount #{1 2})))))

(deftest cartesian-product-test
  (testing "cartesian product with two arguments"
    (is (= {:tag :cartesian-product
            :children [#{1 2} #{2 3}]}
           (bx #{1 2} #{2 3}))))
  (testing "cartesian product with more than two arguments"
    (is (= {:tag :cartesian-product
            :children [{:tag :cartesian-product :children [#{1 2} #{2 3}]}
                       #{3 4}]}
           (bx #{1 2} #{2 3} #{3 4})))))

(deftest set-union-test
  (testing "set union with two arguments"
    (is (= {:tag :set-union
            :children [#{1 2} #{2 3}]}
           (bunion #{1 2} #{2 3}))))
  (testing "set union with more than two arguments"
    (is (= {:tag :set-union
            :children [{:tag :set-union :children [ #{1 2} #{2 3}]}
                       #{3 4}]}
           (bunion #{1 2} #{2 3} #{3 4})))))

(deftest set-intersection-test
  (testing "set intersection with two arguments"
    (is (= {:tag :set-intersection
            :children [#{1 2} #{2 3}]}
           (bintersect #{1 2} #{2 3}))))
  (testing "set intersection with more than two arguments"
    (is (= {:tag :set-intersection
            :children [{:tag :set-intersection :children [ #{1 2} #{2 3}]}
                       #{3 4}]}
           (bintersect #{1 2} #{2 3} #{3 4})))))

(deftest set-difference-test
  (testing "set difference with two arguments"
    (is (= {:tag :set-difference
            :children [#{1 2} #{2 3}]}
           (bset- #{1 2} #{2 3}))))
  (testing "set intersection with more than two arguments"
    (is (= {:tag :set-difference
            :children [{:tag :set-difference :children [ #{1 2} #{2 3}]}
                       #{3 4}]}
           (bset- #{1 2} #{2 3} #{3 4})))))

(deftest member-test
  (testing "member representation with two arguments"
    (is (= {:tag :member
            :children [1 #{1}]}
           (bmember 1 #{1}))))
  (testing "member representation with more than two arguments"
    (is (= {:tag :and :children [{:tag :member :children [1 #{1}]}
                                 {:tag :member :children [1 #{2}]}]}
           (bmember 1 #{1} #{2})))))

(deftest members-test
  (testing "members representation with two arguments"
    (is (= {:tag :member
            :children [1 #{1}]}
           (bmembers #{1} 1))))
  (testing "members representation with more than two arguments"
    (is (= {:tag :and :children [{:tag :member :children [1 #{1}]}
                                 {:tag :member :children [2 #{1}]}]}
           (bmembers #{1} 1 2)))))

(deftest subset-test
  (testing "subset representation with two arguments"
    (is (= {:tag :subset
            :children [#{1} #{2}]}
           (bsubset #{1} #{2})
           (bsuperset #{2} #{1}))))
  (testing "subset representation with more than two arguments"
    (is (= {:tag :and :children [{:tag :subset :children [#{1} #{2}]}
                                 {:tag :subset :children [#{2} #{3}]}]}
           (bsubset #{1} #{2} #{3})
           (bsuperset #{3} #{2} #{1})))))