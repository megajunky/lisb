(ns lisb.translation.ast2lisb
  (:require [lisb.translation.lisb2ir :refer :all])
  (:import (de.be4.classicalb.core.parser.node
             Start
             AAbstractMachineParseUnit
             AMachineMachineVariant
             AMachineHeader
             AVariablesMachineClause
             AInvariantMachineClause
             AInitialisationMachineClause
             ABlockSubstitution
             AAssignSubstitution
             AMemberPredicate
             AIntegerExpression
             ANatSetExpression
             AAddExpression
             AMinusExpression
             AMultOrCartExpression
             AMinusOrSetSubtractExpression
             ADivExpression
             AUnaryMinusExpression
             ABooleanTrueExpression
             ABooleanFalseExpression
             AConvertBoolExpression
             AIdentifierExpression
             AEmptySetExpression
             ASetExtensionExpression
             AComprehensionSetExpression
             APowSubsetExpression
             APow1SubsetExpression
             AFinSubsetExpression
             AFin1SubsetExpression
             ACardExpression
             AUnionExpression
             AIntersectionExpression
             ASetSubtractionExpression
             ANotMemberPredicate
             ASubsetPredicate
             ASubsetStrictPredicate
             ABoolSetExpression
             ANaturalSetExpression
             ANatural1SetExpression
             AIntegerSetExpression
             AIntSetExpression
             ANat1SetExpression
             TIntegerLiteral
             TIdentifierLiteral
             AConjunctPredicate
             ADisjunctPredicate
             ANegationPredicate
             AEqualPredicate
             ANotEqualPredicate
             AEquivalencePredicate
             ALessPredicate
             AGreaterPredicate
             ALessEqualPredicate
             AGreaterEqualPredicate
             AMaxExpression
             AMinExpression
             AModuloExpression
             ACoupleExpression
             ARelationsExpression
             ADomainExpression
             ARangeExpression
             AIdentityExpression
             ADomainRestrictionExpression
             ADomainSubtractionExpression
             ARangeRestrictionExpression
             ARangeSubtractionExpression
             AReverseExpression
             AImageExpression
             AOverwriteExpression
             ADirectProductExpression
             ACompositionExpression
             AParallelProductExpression
             AFirstProjectionExpression
             ASecondProjectionExpression
             AClosureExpression
             AReflexiveClosureExpression
             AIterationExpression
             ATransFunctionExpression
             ATransRelationExpression
             APartialFunctionExpression
             ATotalFunctionExpression
             APartialSurjectionExpression
             ATotalSurjectionExpression
             APartialInjectionExpression
             ATotalInjectionExpression
             APartialBijectionExpression
             ATotalBijectionExpression
             ALambdaExpression
             AFunctionExpression
             AImplicationPredicate
             AForallPredicate
             AExistsPredicate
             AIntervalExpression
             ASequenceExtensionExpression
             AEmptySequenceExpression
             AIseqExpression
             AIseq1Expression
             APermExpression
             AConcatExpression
             AInsertFrontExpression
             AInsertTailExpression
             ARevExpression
             AFirstExpression
             ALastExpression
             AFrontExpression
             ATailExpression
             ARestrictFrontExpression
             ARestrictTailExpression
             APowerOfExpression
             AGeneralSumExpression
             AGeneralProductExpression
             AGeneralUnionExpression
             AGeneralIntersectionExpression
             ASeqExpression
             ASeq1Expression
             AGeneralConcatExpression
             AIfThenElseExpression
             AQuantifiedIntersectionExpression
             AQuantifiedUnionExpression
             ARecEntry
             AStructExpression
             ARecExpression
             ARecordFieldExpression
             AStringExpression
             TStringLiteral
             ADefinitionExpression
             ALetExpressionExpression
             ALetPredicatePredicate
             APredicateParseUnit
             AExpressionParseUnit
             ADefinitionPredicate
             ADefinitionsMachineClause
             ADefinitionFileParseUnit
             APredicateDefinitionDefinition
             AExpressionDefinitionDefinition
             AFileDefinitionDefinition
             AStringSetExpression
             AConstantsMachineClause
             APropertiesMachineClause
             AConstraintsMachineClause
             ASetsMachineClause
             AConcreteVariablesMachineClause
             AAssertionsMachineClause
             AOperationsMachineClause
             ASkipSubstitution
             ABecomesElementOfSubstitution
             ATotalRelationExpression
             ANotSubsetPredicate
             ANotSubsetStrictPredicate
             ASurjectionRelationExpression
             ATotalSurjectionRelationExpression
             ASizeExpression
             AEnumeratedSetSet
             ADeferredSetSet
             ASubstitutionParseUnit
             ABecomesSuchSubstitution
             AOperationCallSubstitution
             AParallelSubstitution
             ASequenceSubstitution
             AAnySubstitution
             ALetSubstitution
             AVarSubstitution
             APreconditionSubstitution
             AAssertionSubstitution
             AIfSubstitution
             AIfElsifSubstitution
             ASuccessorExpression
             APredecessorExpression
             AMinIntExpression
             AMaxIntExpression
             AChoiceSubstitution
             AChoiceOrSubstitution
             ASelectSubstitution
             ASelectWhenSubstitution
             AOperation
             AMachineClauseParseUnit)
           (java.util LinkedList)))

(declare ast->lisb read-bmachine)

(defn lisbify [lisb & nodes]
  (conj (map ast->lisb nodes) lisb))

(defn left-right [lisb node]
  (lisbify lisb (.getLeft node) (.getRight node)))

(defn expression [lisb node]
  (lisbify lisb (.getExpression node)))

(defn concat-last [lisb & nodes]
  (concat (apply lisbify lisb (drop-last nodes)) (map ast->lisb (last nodes))))

(defn collect-left-associative [node]
  (let [left-node (.getLeft node)
        right-node (.getRight node)]
    (if (= (class node) (class left-node))
      (conj (collect-left-associative left-node) right-node)
      [left-node right-node])))

(defn collect-right-associative [node]
  (let [left-node (.getLeft node)
        right-node (.getRight node)]
    (if (= (class node) (class right-node))
      (conj (collect-right-associative right-node) left-node)
      [right-node left-node])))


(defn multi-arity [lisb node]
  (apply (partial lisbify lisb) (collect-left-associative node)))

;;; ast-> lisb start

(defmulti ast->lisb (fn [node] (class node)))

(defmethod ast->lisb Start [node] (ast->lisb (.getPParseUnit node)))


;;; parse units

(defmethod ast->lisb AAbstractMachineParseUnit [node]
  (concat-last 'machine (.getVariant node) (.getHeader node) (.getMachineClauses node))
  #_(conj (ast-list->lisb (.getMachineClauses node)) (ast->lisb (.getHeader node)) (ast->lisb (.getVariant node)) 'machine))
(defmethod ast->lisb AMachineMachineVariant [_]
  '(machine-variant))
(defmethod ast->lisb AMachineHeader [node]
  (lisbify 'machine-header (first (.getName node)) (.getParameters node)))  ; there should be exact one identifier as name!)

(defmethod ast->lisb AMachineClauseParseUnit [node]
  (ast->lisb (.getMachineClause node)))

(defmethod ast->lisb APredicateParseUnit [node]
  (ast->lisb (.getPredicate node)))

(defmethod ast->lisb AExpressionParseUnit [node]
  (ast->lisb (.getExpression node)))

(defmethod ast->lisb ASubstitutionParseUnit [node]
  (ast->lisb (.getSubstitution node)))


;;; machine clauses

(defmethod ast->lisb AConstraintsMachineClause [node]
  (lisbify 'constraints (.getPredicates node)))  ; (.getPredicates node) returns ONE Predicate and no list!

(defmethod ast->lisb ASetsMachineClause [node]
  (concat-last 'sets (.getSetDefinitions node)))
(defmethod ast->lisb ADeferredSetSet [node]
  (lisbify 'deferred-set (first (.getIdentifier node))))  ; there should be exact one identifier
(defmethod ast->lisb AEnumeratedSetSet [node]
  (concat-last 'enumerated-set (first (.getIdentifier node)) (.getElements node)))  ; there should be exact one identifier

; same for concrete constants
(defmethod ast->lisb AConstantsMachineClause [node]
  (concat-last 'constants (.getIdentifiers node)))

(defmethod ast->lisb APropertiesMachineClause [node]
  (lisbify 'properties (.getPredicates node)))  ; (.getPredicates node) returns ONE Predicate and no list!

(defmethod ast->lisb ADefinitionsMachineClause [node]
  (concat-last 'definitions (.getDefinitions node)))
; TODO
#_(defmethod ast->lisb AExpressionDefinitionDefinition [node])
#_(defmethod ast->lisb APredicateDefinitionDefinition [node])

(defmethod ast->lisb AVariablesMachineClause [node]
  (concat-last 'variables (.getIdentifiers node)))

; TODO: concrete variables

(defmethod ast->lisb AInvariantMachineClause [node]
  (lisbify 'invariants (.getPredicates node)))  ; (.getPredicates node) returns ONE Predicate and no list!

(defmethod ast->lisb AAssertionsMachineClause [node]
  (concat-last 'assertions (.getPredicates node)))

(defmethod ast->lisb AInitialisationMachineClause [node]
  (lisbify 'init (.getSubstitutions node)))  ; AInitialisationMachineClause holds one PSubstitution

(defmethod ast->lisb AOperationsMachineClause [node]
  (concat-last 'operations (.getOperations node)))
(defmethod ast->lisb AOperation [node]
  (lisbify 'operation
           (.getReturnValues node)  ; TODO: test if multiple return values are possible
           (first (.getOpName node))  ; there should be exact one identifier
           (.getParameters node)
           (.getOperationBody node)))


;;; substitutions

(defmethod ast->lisb ASkipSubstitution [_]
  'skip)

(defmethod ast->lisb ABlockSubstitution [node]
  (ast->lisb (.getSubstitution node)))

(defmethod ast->lisb AAssignSubstitution [node]
  (let [left (ast->lisb (.getLhsExpression node))
        right (ast->lisb (.getRhsExpressions node))]
    (concat ['assign] (interleave left right))))

; TODO: functional override

(defmethod ast->lisb ABecomesElementOfSubstitution [node]
  (lisbify 'becomes-element-of (.getIdentifiers node) (.getSet node)))

(defmethod ast->lisb ABecomesSuchSubstitution [node]
  (lisbify 'becomes-such (.getIdentifiers node) (.getPredicate node)))

(defmethod ast->lisb AOperationCallSubstitution [node]
  (lisbify 'operation-call (.getResultIdentifiers node) (first (.getOperation node)) (.getParameters node))) ; there should be exact one identifier in .getOperation

(defmethod ast->lisb AParallelSubstitution [node]
  (concat-last 'parallel-substitution (.getSubstitutions node)))

(defmethod ast->lisb ASequenceSubstitution [node]
  (concat-last 'sequence-substitution (.getSubstitutions node)))

(defmethod ast->lisb AAnySubstitution [node]
  (lisbify 'any (.getIdentifiers node) (.getWhere node) (.getThen node)))

(defmethod ast->lisb ALetSubstitution [node]
  (lisbify 'let-sub (.getIdentifiers node) (.getPredicate node) (.getSubstitution node)))

(defmethod ast->lisb AVarSubstitution [node]
  (lisbify 'var-sub (.getIdentifiers node) (.getSubstitution node)))

(defmethod ast->lisb APreconditionSubstitution [node]
  (lisbify 'pre (.getPredicate node) (.getSubstitution node)))

(defmethod ast->lisb AAssertionSubstitution [node]
  (lisbify 'assert (.getPredicate node) (.getSubstitution node)))

(defmethod ast->lisb AChoiceSubstitution [node]
  (concat-last 'choice (.getSubstitutions node)))
(defmethod ast->lisb AChoiceOrSubstitution [node]
  (ast->lisb (.getSubstitution node)))

(defmethod ast->lisb AIfSubstitution [node]
  (let [condition (ast->lisb (.getCondition node))
        then (ast->lisb (.getThen node))
        else-ifs (mapcat identity (ast->lisb (.getElsifSubstitutions node)))
        else (ast->lisb (.getElse node))]
    (if (empty? else-ifs)
      (if else
        (list 'if-sub condition then else)
        (list 'if-sub condition then))
      (if else
        (concat (list 'cond condition then) else-ifs [else])
        (concat (list 'cond condition then) else-ifs)))))
(defmethod ast->lisb AIfElsifSubstitution [node]
  [(ast->lisb (.getCondition node)) (ast->lisb (.getThenSubstitution node))])

(defmethod ast->lisb ASelectSubstitution [node]
  (let [condition (ast->lisb (.getCondition node))
        then (ast->lisb (.getThen node))
        else-ifs (mapcat identity (ast->lisb (.getWhenSubstitutions node)))
        else (ast->lisb (.getElse node))]
    (if else
      (concat (list 'select condition then) else-ifs [else])
      (concat (list 'select condition then) else-ifs))))
(defmethod ast->lisb ASelectWhenSubstitution [node]
  [(ast->lisb (.getCondition node)) (ast->lisb (.getSubstitution node))])

; TODO: case

;;; if-then-else

(defmethod ast->lisb AIfThenElseExpression [node]
  (lisbify 'if-expr (.getCondition node) (.getThen node) (.getElse node)))


;;; let

(defmethod ast->lisb ALetExpressionExpression [node]
  (lisbify 'let-expr (.getIdentifiers node) (.getAssignment node) (.getExpr node)))
(defmethod ast->lisb ALetPredicatePredicate [node]
  (lisbify 'let-pred (.getIdentifiers node) (.getAssignment node) (.getPred node)))


;;; trees


;;; reals - (alpha - besser nicht verwenden)


;;; strings

(defmethod ast->lisb AStringExpression [node]
  (.getText (.getContent node)))

(defmethod ast->lisb AStringSetExpression [_]
  'string-set)


;;; records

(defmethod ast->lisb AStructExpression [node]
  (concat (list 'struct) (mapcat ast->lisb (.getEntries node))))

(defmethod ast->lisb ARecExpression [node]
  (concat (list 'record) (mapcat ast->lisb (.getEntries node))))

(defmethod ast->lisb ARecEntry [node]
  [(ast->lisb (.getIdentifier node)) (ast->lisb (.getValue node))])

(defmethod ast->lisb ARecordFieldExpression [node]
  (lisbify 'rec-get (.getRecord node) (.getIdentifier node)))


;;; sequences

(defmethod ast->lisb AEmptySequenceExpression [_]
  '(sequence))
(defmethod ast->lisb ASequenceExtensionExpression [node]
  (concat-last 'sequence (.getExpression node)))
(defmethod ast->lisb ASeqExpression [node]
  (expression 'seq node))
(defmethod ast->lisb ASeq1Expression [node]
  (expression 'seq1 node))
(defmethod ast->lisb AIseqExpression [node]
  (expression 'iseq node))
(defmethod ast->lisb AIseq1Expression [node]
  (expression 'iseq1 node))
(defmethod ast->lisb APermExpression [node]
  (expression 'perm node))
(defmethod ast->lisb ASizeExpression [node]
  (expression 'count-seq node))
(defmethod ast->lisb AConcatExpression [node]
  (multi-arity 'concat node))
(defmethod ast->lisb AInsertFrontExpression [node]
  (apply (partial lisbify 'cons) (collect-right-associative node)))
(defmethod ast->lisb AInsertTailExpression [node]
  (multi-arity 'append node))
(defmethod ast->lisb ARevExpression [node]
  (expression 'reverse node))
(defmethod ast->lisb AFirstExpression [node]
  (expression 'first node))
(defmethod ast->lisb ALastExpression [node]
  (expression 'last node))
(defmethod ast->lisb AFrontExpression [node]
  (expression 'drop-last node))
(defmethod ast->lisb ATailExpression [node]
  (expression 'rest node))
(defmethod ast->lisb AGeneralConcatExpression [node]
  (expression 'conc node))
(defmethod ast->lisb ARestrictFrontExpression [node]
  (lisbify 'take (.getRight node) (.getLeft node)))
(defmethod ast->lisb ARestrictTailExpression [node]
  (lisbify 'drop (.getRight node) (.getLeft node)))


;;; functions

(defmethod ast->lisb APartialFunctionExpression [node]
  (left-right '+-> node))
(defmethod ast->lisb ATotalFunctionExpression [node]
  (left-right '--> node))
(defmethod ast->lisb APartialSurjectionExpression [node]
  (left-right '+->> node))
(defmethod ast->lisb ATotalSurjectionExpression [node]
  (left-right '-->> node))
(defmethod ast->lisb APartialInjectionExpression [node]
  (left-right '>+> node))
(defmethod ast->lisb ATotalInjectionExpression [node]
  (left-right '>-> node))
(defmethod ast->lisb APartialBijectionExpression [node]
  (left-right '>+>> node))
(defmethod ast->lisb ATotalBijectionExpression [node]
  (left-right '>->> node))
(defmethod ast->lisb ALambdaExpression [node]
  (lisbify 'lambda (.getIdentifiers node) (.getPredicate node) (.getExpression node)))
(defmethod ast->lisb AFunctionExpression [node]
  (let [f (.getIdentifier node)
        params (.getParameters node)]
    (cond
      (= (class f) ASuccessorExpression) (concat-last 'inc params)
      (= (class f) APredecessorExpression) (concat-last 'dec params)
      :else (concat-last 'call f params))))


;;; relations

(defmethod ast->lisb ARelationsExpression [node]
  (multi-arity '<-> node))
(defmethod ast->lisb ATotalRelationExpression [node]
  (multi-arity 'total-relation node))
(defmethod ast->lisb ASurjectionRelationExpression [node]
  (multi-arity 'surjective-relation node))
(defmethod ast->lisb ATotalSurjectionRelationExpression [node]
  (multi-arity 'total-surjective-relation node))
(defmethod ast->lisb ACoupleExpression [node]
  (let [children (mapv ast->lisb (.getList node))
        left (first children)]
    (if (vector? left)
      (conj left (second children))
      children)))
(defmethod ast->lisb ADomainExpression [node]
  (expression 'dom node))
(defmethod ast->lisb ARangeExpression [node]
  (expression 'ran node))
(defmethod ast->lisb AIdentityExpression [node]
  (expression 'identity node))
(defmethod ast->lisb ADomainRestrictionExpression [node]
  (left-right '<| node))
(defmethod ast->lisb ADomainSubtractionExpression [node]
  (left-right '<<| node))
(defmethod ast->lisb ARangeRestrictionExpression [node]
  (left-right '|> node))
(defmethod ast->lisb ARangeSubtractionExpression [node]
  (left-right '|>> node))
(defmethod ast->lisb AReverseExpression [node]
  (expression 'inverse node))
(defmethod ast->lisb AImageExpression [node]
  (left-right 'image node))
(defmethod ast->lisb AOverwriteExpression [node]
  (multi-arity '<+ node))
(defmethod ast->lisb ADirectProductExpression [node]
  (multi-arity '>< node))
(defmethod ast->lisb ACompositionExpression [node]
  (multi-arity 'comp node))
(defmethod ast->lisb AParallelProductExpression [node]
  (multi-arity '|| node))
(defmethod ast->lisb AFirstProjectionExpression [node]
  (list 'prj1 (ast->lisb (.getExp1 node)) (ast->lisb (.getExp2 node))))
(defmethod ast->lisb ASecondProjectionExpression [node]
  (list 'prj2 (ast->lisb (.getExp1 node)) (ast->lisb (.getExp2 node))))
(defmethod ast->lisb AClosureExpression [node]
  (expression 'closure1 node))
(defmethod ast->lisb AReflexiveClosureExpression [node]
  (expression 'closure node))
(defmethod ast->lisb AIterationExpression [node]
  (left-right 'iterate node))
(defmethod ast->lisb ATransFunctionExpression [node]
  (expression 'fnc node))
(defmethod ast->lisb ATransRelationExpression [node]
  (expression 'rel node))


;;; numbers

(defmethod ast->lisb AIntegerExpression [node]
  (Long/parseLong (.getText (.getLiteral node))))
(defmethod ast->lisb AUnaryMinusExpression [node]
  (let [expr (ast->lisb (.getExpression node))]
    (if (number? expr)
      (- expr)
      (list '- expr))))
(defmethod ast->lisb AIntegerSetExpression [_] 'integer-set)
(defmethod ast->lisb ANaturalSetExpression [_] 'natural-set)
(defmethod ast->lisb ANatural1SetExpression [_] 'natural1-set)
(defmethod ast->lisb AIntSetExpression [_] 'int-set)
(defmethod ast->lisb ANatSetExpression [_] 'nat-set)
(defmethod ast->lisb ANat1SetExpression [_] 'nat1-set)
(defmethod ast->lisb AIntervalExpression [node]
  (lisbify 'interval (.getLeftBorder node) (.getRightBorder node)))
(defmethod ast->lisb AMinIntExpression [_]
  'min-int)
(defmethod ast->lisb AMaxIntExpression [_]
  'max-int)
(defmethod ast->lisb ALessPredicate [node]
  (multi-arity '< node))
(defmethod ast->lisb AGreaterPredicate [node]
  (multi-arity '> node))
(defmethod ast->lisb ALessEqualPredicate [node]
  (multi-arity '<= node))
(defmethod ast->lisb AGreaterEqualPredicate [node]
  (multi-arity '>= node))
(defmethod ast->lisb AMaxExpression [node]
  (expression 'max node))
(defmethod ast->lisb AMinExpression [node]
  (expression 'min node))
(defmethod ast->lisb AAddExpression [node]
  (multi-arity '+ node))
(defmethod ast->lisb AMinusOrSetSubtractExpression [node]
  (multi-arity '- node))
(defmethod ast->lisb AMultOrCartExpression [node]
  (multi-arity '* node))
(defmethod ast->lisb ADivExpression [node]
  (multi-arity '/ node))
(defmethod ast->lisb APowerOfExpression [node]
  (left-right '** node))
(defmethod ast->lisb AModuloExpression [node]
  (multi-arity 'mod node))
(defmethod ast->lisb AGeneralProductExpression [node]
  (lisbify 'pi (.getIdentifiers node) (.getPredicates node) (.getExpression node)))  ; (.getPredicates node) returns ONE Predicate and no list!
(defmethod ast->lisb AGeneralSumExpression [node]
  (lisbify 'sigma (.getIdentifiers node) (.getPredicates node) (.getExpression node)))  ; (.getPredicates node) returns ONE Predicate and no list!
; ASuccessorExpression - processed in AFunctionExpression
; APredecessorExpression - processed in AFunctionExpression


;;; sets

(defmethod ast->lisb AEmptySetExpression [_]
  #{})

(defmethod ast->lisb ASetExtensionExpression [node]
  (into #{} (map ast->lisb (.getExpressions node))))

(defmethod ast->lisb AComprehensionSetExpression [node]
 (lisbify 'comp-set (.getIdentifiers node) (.getPredicates node)))  ; .getPredicates returns ONE predicate)

(defmethod ast->lisb APowSubsetExpression [node]
  (expression 'pow node))

(defmethod ast->lisb APow1SubsetExpression [node]
  (expression 'pow1 node))

(defmethod ast->lisb AFinSubsetExpression [node]
  (expression 'fin node))

(defmethod ast->lisb AFin1SubsetExpression [node]
  (expression 'fin1 node))

(defmethod ast->lisb ACardExpression [node]
  (expression 'count node))

(defmethod ast->lisb AUnionExpression [node]
  (multi-arity 'union node))

(defmethod ast->lisb AIntersectionExpression [node]
  (multi-arity 'intersection node))

(defmethod ast->lisb ASetSubtractionExpression [node]
  (multi-arity 'difference node))

(defmethod ast->lisb AMemberPredicate [node]
  (lisbify 'contains? (.getRight node) (.getLeft node)))

(defmethod ast->lisb ANotMemberPredicate [node]
  (list 'not (lisbify 'contains? (.getRight node) (.getLeft node))))

(defmethod ast->lisb ASubsetPredicate [node]
  (left-right 'subset? node))

(defmethod ast->lisb ANotSubsetPredicate [node]
  (list 'not (left-right 'subset? node)))

(defmethod ast->lisb ASubsetStrictPredicate [node]
  (left-right 'subset-strict? node))

(defmethod ast->lisb ANotSubsetStrictPredicate [node]
  (list 'not (left-right 'subset-strict? node)))

(defmethod ast->lisb AGeneralUnionExpression [node]
  (expression 'unite-sets node))

(defmethod ast->lisb AGeneralIntersectionExpression [node]
  (expression 'intersect-sets node))

(defmethod ast->lisb AQuantifiedUnionExpression [node]
  (lisbify 'union-pe (.getIdentifiers node) (.getPredicates node) (.getExpression node)))

(defmethod ast->lisb AQuantifiedIntersectionExpression [node]
  (lisbify 'intersection-pe (.getIdentifiers node) (.getPredicates node) (.getExpression node)))

;;; booleans

(defmethod ast->lisb ABooleanTrueExpression [_]
  true)

(defmethod ast->lisb ABooleanFalseExpression [_]
  false)

(defmethod ast->lisb ABoolSetExpression [_]
  'bool-set)

(defmethod ast->lisb AConvertBoolExpression [node]
  (lisbify 'pred->bool (.getPredicate node)))


;;; equality predicates

(defmethod ast->lisb AEqualPredicate [node]
  (left-right '= node))

(defmethod ast->lisb ANotEqualPredicate [node]
  (left-right 'not= node))


;;; logical predicates

(defmethod ast->lisb AConjunctPredicate [node]
  (multi-arity 'and node))

(defmethod ast->lisb ADisjunctPredicate [node]
  (multi-arity 'or node))

(defmethod ast->lisb AImplicationPredicate [node]
  (multi-arity '=> node))

(defmethod ast->lisb AEquivalencePredicate [node]
  (multi-arity '<=> node))

(defmethod ast->lisb ANegationPredicate [node]
  (lisbify 'not (.getPredicate node)))

(defmethod ast->lisb AForallPredicate [node]
  (lisbify 'for-all (.getIdentifiers node) (.getImplication node)))

(defmethod ast->lisb AExistsPredicate [node]
  (lisbify 'exists (.getIdentifiers node) (.getPredicate node)))


;;; identifier

(defmethod ast->lisb AIdentifierExpression [node]
  (ast->lisb (first (.getIdentifier node))))   ; Es sollte exakt nur ein Identifier in einer Identifier Expression sein

(defmethod ast->lisb TIdentifierLiteral [node]
  (keyword (.getText node)))


;;; misc

(defmethod ast->lisb nil [_] nil)

; for the most part i want a vector
(defmethod ast->lisb LinkedList [node]
  (mapv ast->lisb node))
