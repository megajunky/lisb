(ns lisb.integration-test
  (:require [clojure.test :refer :all])
  (:require [lisb.core :refer [eval state-space]])
  (:require [lisb.representation :refer :all])
  (:require [lisb.translation :refer [to-ast]]))

(deftest integration
  (testing "no exception is thrown when executing implemented command;
            this test only checks that a valid (i.e. accepted) AST is constructed
            without concern about semantics
            for now only 'truish' predicates are evaluated because eval cannot handle
            ComputationNotCompletedResults yet"
    (let [ss (state-space)]
      (is (eval ss (to-ast (b= 1 1))))
      (is (eval ss (to-ast (b= :x 1))))
      (is (eval ss (to-ast (b= 1 1 1))))
      (is (eval ss (to-ast (b= :x true))))
      (is (eval ss (to-ast (b= :x false))))

      (is (eval ss (to-ast (bnot= :x 1))))
      (is (eval ss (to-ast (bnot= :x 1 2))))

      (is (eval ss (to-ast (b< 1 2))))
      (is (eval ss (to-ast (b< 1 2 3))))

      (is (eval ss (to-ast (b> 2 1))))
      (is (eval ss (to-ast (b> 3 2 1))))

      (is (eval ss (to-ast (b<= 1 2))))
      (is (eval ss (to-ast (b<= 1 1 3))))

      (is (eval ss (to-ast (b>= 2 1))))
      (is (eval ss (to-ast (b>= 3 3 1))))

      (is (eval ss (to-ast (b< 1 (b+ 1 2)))))
      (is (eval ss (to-ast (b< 1 (b+ 1 2 3)))))

      (is (eval ss (to-ast (b< 1 (b- 10 2)))))
      (is (eval ss (to-ast (b< 1 (b- 10 2 3)))))

      (is (eval ss (to-ast (b= 6 (b* 2 3)))))
      (is (eval ss (to-ast (b= 24 (b* 2 3 4)))))

      (is (eval ss (to-ast (b= 6 (bdiv 24 4)))))
      (is (eval ss (to-ast (b= 2 (bdiv 24 4 3)))))

      (is (eval ss (to-ast (band (b< 1 2) (b< 2 3)))))
      (is (eval ss (to-ast (band (b< 1 2) (b< 2 3) (b< 3 4)))))

      (is (eval ss (to-ast (bor (b< 1 2) (b< 2 3)))))
      (is (eval ss (to-ast (bor (b< 1 2) (b< 2 3) (b< 3 4)))))

      (is (eval ss (to-ast (b<=> (b< 1 2) (b< 2 3)))))
      (is (eval ss (to-ast (b<=> (b< 1 2) (b< 2 3) (b< 3 4)))))

      (is (eval ss (to-ast (bnot (b< 2 1)))))

      (is (eval ss (to-ast (b= true (bpred->bool (b< 1 2))))))

      (is (eval ss (to-ast (b= :x #{1 2}))))
      (is (eval ss (to-ast (b= :x #{1 2 (b+ 1 2)}))))

      (is (eval ss (to-ast (b= #{1 2 3} (bset [:x] (b< 0 :x 4))))))
      
      (is (eval ss (to-ast (b= :x (bpow #{1 2})))))

      (is (eval ss (to-ast (b= :x (bpow1 #{1 2})))))

      (is (eval ss (to-ast (b= :x (bfin #{1 2})))))

      (is (eval ss (to-ast (b= :x (bfin1 #{1 2})))))

      (is (eval ss (to-ast (b= 2 (bcount #{1 2})))))

      (is (eval ss (to-ast (b= #{} (bx #{} #{})))))

      ;; FIXME: without the additional set the translator breaks
      (is (eval ss (to-ast (b= :x #{(bx #{1 2} #{2 3})}))))

      (is (eval ss (to-ast (b= :x #{(bx #{1 2} #{2 3} #{3 4})}))))

      (is (eval ss (to-ast (b= #{1 2 3} (bunion #{1 2} #{2 3})))))
      (is (eval ss (to-ast (b= #{1 2 3 4} (bunion #{1 2} #{2 3} #{3 4})))))

      (is (eval ss (to-ast (b= #{2} (bintersect #{1 2} #{2 3})))))
      (is (eval ss (to-ast (b= #{3 4} (bintersect #{1 2 3 4} #{2 3 4} #{3 4})))))

      (is (eval ss (to-ast (b= #{1} (bset- #{1 2} #{2 3})))))
      (is (eval ss (to-ast (b= #{1} (bset- #{1 2} #{2 3} #{3 4})))))

      (is (eval ss (to-ast (bmember 1 #{1}))))
      (is (eval ss (to-ast (bmember 1 #{1} #{1 2}))))

      (is (eval ss (to-ast (bmembers #{1 2 3} 1))))
      (is (eval ss (to-ast (bmembers #{1 2 3} 1 2))))

      (is (eval ss (to-ast (bsubset #{1} #{1 2}))))
      (is (eval ss (to-ast (bsubset #{1} #{1 2} #{1 2 3}))))

      (is (eval ss (to-ast (bsuperset #{1 2} #{1}))))
      (is (eval ss (to-ast (bsuperset #{1 2 3} #{1 2} #{1}))))

      (is (eval ss (to-ast (bsubset-strict #{1} #{1 2}))))
      (is (eval ss (to-ast (bsubset-strict #{1} #{1 2} #{1 2 3}))))

      (is (eval ss (to-ast (bsuperset-strict #{1 2} #{1}))))
      (is (eval ss (to-ast (bsuperset-strict #{1 2 3} #{1 2} #{1}))))

      (is (eval ss (to-ast (bmember true (bbool-set)))))
      (is (eval ss (to-ast (bmember 0 (bnatural-set)))))
      (is (eval ss (to-ast (bmember 1 (bnatural1-set)))))
      (is (eval ss (to-ast (bmember -1 (bint-set)))))
      (is (eval ss (to-ast (bmember 0 (bnat-set)))))
      (is (eval ss (to-ast (bmember 1 (bnat1-set)))))

      (is (eval ss (to-ast (b= 3 (bmax #{1 2 3})))))
      (is (eval ss (to-ast (b= 3 (bmax 1 2 3)))))
      (is (eval ss (to-ast (b= 3 (bmax 2 3)))))

      (is (eval ss (to-ast (b= 1 (bmin #{1 2 3})))))
      (is (eval ss (to-ast (b= 1 (bmin 1 2 3)))))
      (is (eval ss (to-ast (b= 2 (bmin 2 3)))))

      (is (eval ss (to-ast (b= 2 (bmod 5 3)))))

      (is (eval ss (to-ast (b= 2 (binc 1)))))

      (is (eval ss (to-ast (b= 0 (bdec 1)))))
      
      (is (eval ss (to-ast (b= :x #{[1 2]}))))

      (is (eval ss (to-ast (bmember :x (b<-> #{1 2} #{3 4})))))
      (is (eval ss (to-ast (bmember :x (b<-> #{1 2} #{3 4} #{5 6})))))

      (is (eval ss (to-ast (b= #{1 2} (bdom #{[1 0] [1 1] [2 42]})))))

      (is (eval ss (to-ast (b= #{0 1 42} (bran #{[1 0] [1 1] [2 42]})))))

      (is (eval ss (to-ast (b= #{[1 1]} (bid #{1})))))

      (is (eval ss (to-ast (b= #{[1 1]} (b<| #{1 2} #{[1 1] [3 0]})))))

      (is (eval ss (to-ast (b= #{[3 0]} (b<<| #{1 2} #{[1 1] [3 0]})))))

      (is (eval ss (to-ast (b= #{[1 1]} (b|> #{[1 1] [3 0]} #{1 2})))))

      (is (eval ss (to-ast (b= #{[3 0]} (b|>> #{[1 1] [3 0]} #{1 2})))))

      (is (eval ss (to-ast (b= #{[3 0]} (binverse #{[0 3]})))))

      (is (eval ss (to-ast (b= #{2} (bimage #{[1 2] [0 3]} #{1 2})))))

      (is (eval ss (to-ast (b= #{[0 0]} (b<+ #{[0 1]} #{[0 0]})))))
      (is (eval ss (to-ast (b= #{[0 0] [1 0]} (b<+ #{[0 1]} #{[0 0]} #{[1 0]})))))

      (is (eval ss (to-ast (b= #{[0 [1 1]] [0 [2 1]]} (b>< #{[0 1] [0 2]} #{[0 1]})))))
      (is (eval ss (to-ast (b= #{[0 [[1 1] 3]] [0 [[2 1] 3]]} (b>< #{[0 1] [0 2]} #{[0 1]} #{[0 3]})))))


      (is (eval ss (to-ast (b= #{[0 0]} (bcomp #{[0 1]} #{[1 0]})))))
      (is (eval ss (to-ast (b= #{[0 5]} (bcomp #{[0 1]} #{[1 2]} #{[2 5]})))))

      (is (eval ss (to-ast (b= #{[[0 0] [1 1]] [[0 0] [2 1]]} (b|| #{[0 1] [0 2]} #{[0 1]})))))
      (is (eval ss (to-ast (b= :a (b|| #{[0 1] [0 2]} #{[0 1]} #{[1 1]}))))) ;; if it works, that's good enough for me

      (is (eval ss (to-ast (b= #{[[1 3] 1] [[1 4] 1]} (bprj1 #{1} #{3, 4})))))
      (is (eval ss (to-ast (b= #{[[1 3] 3] [[1 4] 4]} (bprj2 #{1} #{3, 4})))))

      (is (eval ss (to-ast (b= #{[1 1] [1 2] [2 2]} (bclosure #{[1 2]})))))
      (is (eval ss (to-ast (b= #{[1 2]} (bclosure1 #{[1 2]})))))

      )))

