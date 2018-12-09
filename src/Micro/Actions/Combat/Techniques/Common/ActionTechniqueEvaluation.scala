package Micro.Actions.Combat.Techniques.Common

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ActionTechniqueEvaluation(
  val unit: FriendlyUnitInfo,
  val technique: ActionTechnique) {
  
  lazy val applicabilitiesOther     : Vector[Double]  = ActionTechniqueEvaluator.applicabilitiesOther(unit, technique)
  lazy val totalApplicabilityOther  : Double          = ActionTechniqueEvaluator.totalApplicabilitiesOther(unit, technique)
  lazy val totalApplicabilitySelf   : Double          = ActionTechniqueEvaluator.totalApplicabilitySelf(unit, technique)
  lazy val totalApplicability       : Double          = ActionTechniqueEvaluator.totalApplicability(unit, technique)
}
