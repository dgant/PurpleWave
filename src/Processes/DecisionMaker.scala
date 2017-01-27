package Processes

import Types.Decisions.Decision
import Types.Plans.Plan

import scala.collection.mutable.HashMap

class DecisionMaker {

  var _decisionsByPlan:HashMap[Plan, Decision] = HashMap.empty

  def makeDecisions(plans:Iterable[Plan]):Iterable[Decision] = {
    val possiblePlans = plans.filter(_isPlanPossible).toSeq
    _decisionsByPlan.keys.toSeq.diff(possiblePlans).foreach(_abortPlan)
    possiblePlans.foreach(_implementPlan)
    possiblePlans.map(p => _decisionsByPlan.get(p).get)
  }

  def _isPlanPossible(plan:Plan):Boolean = {
    true
  }

  def _implementPlan(plan:Plan) {
    if (_decisionsByPlan.contains(plan)) {
      return
    }
    var decision = new Decision(plan)
    _decisionsByPlan.put(plan, decision)
  }

  def _abortPlan(plan:Plan) {
    _decisionsByPlan
      .get(plan)
      .foreach(d => {
        _decisionsByPlan -= plan
      })
  }
}
