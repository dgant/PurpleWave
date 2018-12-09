package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique

abstract class GeneralizedWeightedMean(technique: ActionTechnique) extends WeightedActivator(technique) {
  
  protected val tension: Double
  
  override protected def aggregate(applicabilitySignificance: Seq[(Double, Double)]): Option[Double] = {
    /*
    Generalized weighted mean:
    https://www.jstor.org/stable/2099874?seq=1#page_scan_tab_contents
    
    1. Normalize weights to sum to 1
    2. Sum(weight * element^r)^1/r
    3. Use large R for max, small R for min
    */
    var aggregateApplicability = 0.0
    var totalSignificance = 0.0

    applicabilitySignificance.foreach(as => {
      aggregateApplicability += Math.pow(as._1, tension) * as._2
      totalSignificance += as._2
    })

    if (totalSignificance <= 0.0) return None
    val output = Math.pow(aggregateApplicability / totalSignificance, 1.0 / tension)
    Some(output)
  }
}
