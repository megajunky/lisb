(ns lisb.translation.eventb.util
  (:require [potemkin :refer [import-vars]]
            [lisb.prob.animator :refer [api injector]]
            [lisb.translation.eventb dsl ir2eventb eventb2lisb]
            [clojure.walk :refer [walk]])
  (:import
   de.prob.model.eventb.translate.ModelToXML
   (de.prob.model.eventb
    EventBModel
    EventBMachine
    Context)
   (de.prob.model.representation
    DependencyGraph
    DependencyGraph$ERefType)
   (de.prob.animator.domainobjects
    FormulaExpand
    EventB)
   ))

(import-vars [lisb.translation.eventb.dsl eventb])
(import-vars [lisb.translation.eventb.ir2eventb ir->prob ir-pred->str ir-expr->str])
(import-vars [lisb.translation.eventb.eventb2lisb prob->lisb])

(def modelCreator (.getProvider injector EventBModel))

(defn lisb->ir [lisb]
  (eval `(eventb ~lisb)))

(defn model-with-context [model context]
  (reduce (fn [model extended]
            (.addRelationship model (.getName context) (.getName extended) DependencyGraph$ERefType/EXTENDS))
          (.addContext model context)
          (.getExtends context)))

(defn model-with-machine [model machine]
  (let [refined (.getRefinesMachine machine)
        model (reduce (fn [model context]
                        (.addRelationship model (.getName machine) (.getName context) DependencyGraph$ERefType/SEES))
                      (.addMachine model machine)
                      (.getSees machine))]
    (if refined
      (.addRelationship model (.getName machine) (.getName refined) DependencyGraph$ERefType/REFINES)
      model)))

(defn prob-model [& machines-or-contexts]
  (reduce (fn [model value]
            (condp = (type value)
              de.prob.model.eventb.Context (model-with-context model value)
              de.prob.model.eventb.EventBMachine (model-with-machine model value)))
          (.get modelCreator) machines-or-contexts))

(defn ir->prob-model [& ir] (->> ir (map ir->prob) (apply prob-model)))


(defn prob-model->rodin [model model-name path]
  (.writeToRodin (ModelToXML.) model model-name path))

(defn rodin->prob-model [filename]
  (-> (.eventb_load api filename)
      .getModel))


(defn rodin->lisb [filename] (-> filename rodin->prob-model prob->lisb))

(defn get-type
   [ss ir-formula]
    (let [formula (ir-expr->str ir-formula)
        ee (EventB. formula FormulaExpand/EXPAND)]
      (.getType (.typeCheck ss ee))))

(def get-statespace
  (memoize (fn [ir]
             (let [machine (ir->prob ir)
                   model (prob-model machine)]
               (.load model machine {})))))

(comment
  (def ir (eventb (machine :foo
                           (variables :s :t)
                           (invariants
                            (subset? :s (cartesian-product nat-set bool-set))
                            (subset? :t nat-set))
                           (init
                            (assign
                             :s #{}
                             :t #{1, 2})))))

  (get-type (get-statespace ir) (eventb (cartesian-product :s :t)))

  (def model (rodin->prob-model  "../../bachelor-rodin/ABZ2020_v4/PitmanController2_TIME.bum"))
  (prob->lisb (second (.getContexts model)))

  (def new-model (->> model
                      prob->lisb
                      lisb->ir
                      (apply ir->prob-model)))

  (->> (with-out-str
         (doseq [machine (rodin->lisb "../../bachelor-rodin/ABZ2020_v4/PitmanController2_TIME.bum" )]
           (clojure.pprint/pprint (list 'def (list 'eventb machine)))))
       (spit "src/lisb/examples/eventb/abz2020.clj"))
  )

