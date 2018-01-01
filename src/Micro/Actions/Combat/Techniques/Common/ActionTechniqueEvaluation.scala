package Micro.Actions.Combat.Techniques.Common

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ActionTechniqueEvaluation(
  val unit: FriendlyUnitInfo,
  val technique: ActionTechnique) {
  
  lazy val applicabilitiesOther     : Vector[Double]  = unit.matchups.others.flatMap(technique.applicabilityOther(unit, _))
  lazy val totalApplicabilityOther  : Double          = technique.activator(applicabilitiesOther.map(PurpleMath.clampToOne)).getOrElse(0.0)
  lazy val totalApplicabilitySelf   : Double          = PurpleMath.clampToOne(technique.applicabilitySelf(unit))
  lazy val totalApplicability       : Double          = PurpleMath.clampToOne(technique.applicabilityBase) * totalApplicabilitySelf * totalApplicabilityOther
}
