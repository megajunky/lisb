(ns lisb.translation.eventb.ir2eventb-test
  (:require [lisb.translation.util :refer [b]]
            [lisb.translation.eventb.ir2eventb :refer :all]
            [clojure.test :refer [are deftest is run-tests testing]]
            ))

(deftest expr-test
  (testing "Expressions"
    (are [eventb ir] (= eventb (ir-expr->str ir))
      "x+E" (b (+ :x :E))
      "2*(x+E)" (b (* 2 (+ :x :E)))
      "1/3" (b (/ 1 3))
      "{}" (b #{})
      "{1}" (b #{1})
      "{1,x}" (b #{:x 1}) ;;enumerated sets are not orderd!
      )))

(deftest pred-test
  (testing "Predicates"
    (are [eventb ir] (= eventb (ir-pred->str ir))
      "x<10&y>0" (b (and (< :x 10) (> :y 0)))
      "a<<:b&b<<:c" (b (strict-subset? :a :b :c))
      "1=x or 1=1" (b (or (= 1 :x) (= 1 1)))
      )))


(defn action-code [a] (-> a .getCode .getCode))
(defn guard-code [g] (-> g .getPredicate .getCode))

(comment
  "don't allow nested selects"
  (extract-actions (b (select (< :i 10)
                              (|| (assign :i (+ :i 1))
                                  (select (< :x 10) (assign :x 10)))))))
(deftest action-test
  (are [actions ir] (= actions (map action-code (extract-actions ir)))
    ["x := 1"] (b (assign :x 1))
    ["f(x) := 1"] (b (assign (fn-call :f :x) 1))
    ["x,y := 1,x"] (b (assign :x 1 :y :x))
    ["x := 1" "y := TRUE"] (b (|| (assign :x 1) (assign :y true)))
    ["z := 3" "w := x" "x := 1" "y := 2"] (b (|| (|| (assign :z 3) (assign :w :x)) (assign :x 1) (assign :y 2)))
    ["hello := 0"] (b (select (> :x 2) (assign :hello 0))))
    )

(defn find-first-by-name [event-name events]
 (->> events
       (filter (fn [x] (= event-name (.getName x))))
       first))

(defn get-actions [event]
  (->> event
       .getActions
       (map action-code)))

(defn get-guards [event]
  (->> event
       .getGuards
       (map guard-code)
       ))

(deftest prob-machine-test
  (let [ir (b (machine :hello-world
                       (variables :x :y :hello)
                       (invariants
                        (in :hello bool-set)
                        (<= :x 10)
                        (in :y nat-set))
                       (init
                        (assign :x 0 :y 50)
                        (assign :hello true))
                       (operations
                        (:inc [] (pre (< :x 10) (assign :x (+ :x 1))))
                        (:hello [] (assign :hello false)))))
        machine (ir->prob-machine ir)
        events (.getEvents machine)
        invariants (.getInvariants machine)]
    (is (= ["x" "y" "hello"] (map (fn [x] (.getName x)) (.getVariables machine))))
    (is (= ["hello:BOOL" "x<=10" "y:NAT"] (map (fn [x] (.getCode (.getPredicate x))) invariants)))
    (is (= ["INITIALISATION" "inc" "hello"] (map (fn [x] (.getName x)) events)))
    (is (= ["x,y := 0,50" "hello := TRUE"] (get-actions (find-first-by-name "INITIALISATION" events))))
    (is (= ["x<10"] (get-guards (find-first-by-name "inc" events))))
    (is (= ["x := x+1"] (get-actions (find-first-by-name "inc" events))))
    (is (= [] (get-guards (find-first-by-name "hello" events))))
    (is (= ["hello := FALSE"] (get-actions (find-first-by-name "hello" events))))
    ))


(deftest prob-return-as-variable-test
  (let [ir (b (machine :hello-world
                       (variables :i)
                       (invariants
                        (in :i nat-set))
                       (init
                        (assign :i 0))
                       (operations
                        (:inc [] (assign :i (+ :i 1)))
                        (<-- [:result] (:res [] (assign :result :i))))))
        machine (ir->prob-machine ir)
        events (.getEvents machine)
        invariants (.getInvariants machine)]
    (is (= ["i" "result"] (map (fn [x] (.getName x)) (.getVariables machine))))
    (is (= ["i:NAT" "result:NAT"] (map (fn [x] (.getCode (.getPredicate x))) invariants)))
    (is (= ["INITIALISATION" "inc" "res"] (map (fn [x] (.getName x)) events)))
    (is (= ["i := i+1"] (get-actions (find-first-by-name "inc" events))))
    (is (= ["result := i"] (get-actions (find-first-by-name "res" events))))
    ))
