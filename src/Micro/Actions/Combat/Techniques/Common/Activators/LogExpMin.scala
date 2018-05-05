package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique

class LogExpMin(technique: ActionTechnique) extends WeightedActivator(technique) {
  override protected def aggregate(applicabilityAndSignificance: Seq[(Double, Double)]): Double = {
    val numerator = applicabilityAndSignificance.map(p => p._1 * p._2).sum
    val denominator = applicabilityAndSignificance.map(_._2).sum
    numerator / denominator
  }
}
