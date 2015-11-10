(ns lisb.core
  (:import de.prob.Main
           de.prob.scripting.Api
           de.prob.animator.command.CbcSolveCommand
           de.prob.animator.domainobjects.ClassicalB
           (de.be4.classicalb.core.parser.node Start
                                               EOF
                                               Node
                                               AAddExpression
                                               APredicateParseUnit
                                               AIntegerExpression
                                               AIdentifierExpression
                                               TIntegerLiteral
                                               TIdentifierLiteral
                                               AConjunctPredicate
                                               ALessPredicate)))


(defn get-api [] (.getInstance (Main/getInjector) Api))

(defn create-empty-machine []
  (let [tf (java.io.File/createTempFile "evalb" ".mch" nil)
        tn (.getAbsolutePath tf)
        ]
    (.deleteOnExit tf)
    (spit tf "MACHINE empty \n END")
    tn))

(defn state-space []
  (let [machine (create-empty-machine)
        api (get-api)]
    (.b_load api machine)))


(defn predicate [ast]
  (let [p (APredicateParseUnit. ast)
        start (Start. p (EOF.))]
    (ClassicalB. start)))

(defn identifier [n]
  (let [token (TIdentifierLiteral. n)]
    (AIdentifierExpression. [token])))

(defn integer [n]
  (let [token (TIntegerLiteral. (str n))]
    (AIntegerExpression. token)))


(defn conjunct [l r]
  (AConjunctPredicate. l r))


(defn plus [l r]
  (AAddExpression. l r))


(defn less [l r]
  (ALessPredicate. l r))

(defn node?
  "checks whether x already is an AST node"
  [x]
  (instance? Node x))

(defn literal [x]
  (cond (number? x) (integer x)
        (keyword? x) (identifier (name x))))


(defn process-literals [arglist]
  (map (fn [x] (if (node? x)
                   x
                   (literal x)))
       arglist))

(defn chain 
  "chains a list of nodes [n1 n2 n3 ...] into (and (f n1 n2) (and (f n2 n3) (and (f n3 ...))))"
  [f nodes]
  (let [tuples (partition 2 1 nodes)]
    (reduce conjunct (map (partial apply f) tuples))))

(defn b<
  "generates a B AST equivalent to (and (< arg1 arg2) (and (< arg2 arg3) (and (...))))"
  [& args]
  (let [nodes (process-literals args)]
    (chain less nodes)))

(defn b+
  "generates a B AST equivalent to (+ arg1 (+ arg2 (+ arg3 (+ ... argN))))"
  [& args]
  (let [nodes (process-literals args)]
    (reduce plus nodes)))

(defn eval [state-space ast]
  (let [cmd (CbcSolveCommand. (predicate ast))
        _ (.execute state-space cmd)
        free (.getFreeVariables cmd)
        result (.. cmd getValue translate)
        ]
    (when (.. result getValue booleanValue)
      (into {} (map (fn [k][k (.getSolution result k)]) free)))))

