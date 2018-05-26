package Micro.Actions.Combat.Techniques.Common.Activators

import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.ActionTechnique

class WeightedMean(technique: ActionTechnique) extends WeightedActivator(technique) {
  override protected def aggregate(applicabilityAndSignificance: Seq[(Double, Double)]): Double = {
    val numerator   = applicabilityAndSignificance.map(p => p._1 * p._2).sum
    val denominator = applicabilityAndSignificance.map(_._2).sum
    val output      = PurpleMath.nanToOne(numerator / denominator)
    output
  }
}
