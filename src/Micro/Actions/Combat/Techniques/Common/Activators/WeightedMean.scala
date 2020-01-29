package Micro.Actions.Combat.Techniques.Common.Activators

import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.ActionTechnique

class WeightedMean(technique: ActionTechnique) extends WeightedActivator(technique) {
  override protected def aggregate(applicabilityAndSignificance: Seq[(Double, Double)]): Option[Double] = {
    val output = PurpleMath.weightedMean(applicabilityAndSignificance)
    if (output.isNaN) None else Some(output)
  }
}
