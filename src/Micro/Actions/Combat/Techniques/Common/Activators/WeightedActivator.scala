package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class WeightedActivator(technique: ActionTechnique) extends Activator {
  
  final def apply(unit: FriendlyUnitInfo, others: Seq[UnitInfo]): Option[Double] = {
    val applicabilitySignificance = others
      .map(other => (
        technique.applicabilityOther(unit, other),
        technique.significanceOther(unit, other)
      ))
      .filter(_._1.isDefined)
      .map(pair => (pair._1.get, pair._2))
    
    if (applicabilitySignificance.map(_._1).sum == 0.0) return None
    
    Some(aggregate(applicabilitySignificance))
  }
  
  protected def aggregate(applicabilitySignificance: Seq[(Double, Double)]): Double
}
