(ns lisb.high-level
  (:use [lisb.prob.animator])
  (:use [lisb.prob.retranslate])
  (:use [lisb.translation.util])
  (:use [lisb.core])
  (:import de.prob.statespace.Trace))

(defn load-machine-trace [m]
  (let [ss (state-space! (b->ast m))]
    (Trace. ss)))

(defn load-initialized-machine-trace [m]
  (let [ss (state-space! (b->ast m))]
    (.addTransitionWith (Trace. ss) "$initialise_machine" [])))

(defn latest-state [trace]
  (last (.getTransitionList trace)))

(defn root [trace]
  (-> trace (.getStateSpace) (.getRoot)))

(defn perform [trace operation & args]
  (.addTransitionWith trace (name operation) (vec args)))

(defn possible-ops [trace]
  (.getNextTransitions trace))

(defn load-mch!
  ([filename]
   (let [input-string (slurp filename)
         ast (b->ast input-string)]
     {:ir (ast->ir ast)
      :ss (state-space! ast)
      :meta {}}))
   ([filename meta-data]
    (let [input-string (slurp filename)
          ast (b->ast input-string)]
      {:ir (ast->ir ast)
       :ss (state-space! ast)
       :meta meta-data})))

(defn make-mch!
  ([ir]
  {:ir ir
   :ss (state-space! (ir->ast ir))
   :meta {}})
  ([ir meta-data]
   {:ir ir
    :ss (state-space! (ir->ast ir))
    :meta meta-data}))

(defn save-mch!
  [ir target-filename]
  (spit target-filename (ir->b ir)))
