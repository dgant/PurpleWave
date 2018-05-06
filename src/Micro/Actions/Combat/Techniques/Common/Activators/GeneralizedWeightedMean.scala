package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique

abstract class GeneralizedWeightedMean(technique: ActionTechnique) extends WeightedActivator(technique) {
  
  protected val tension: Double
  
  override protected def aggregate(applicabilitySignificance: Seq[(Double, Double)]): Double = {
    /*
    Generalized weighted mean:
    https://www.jstor.org/stable/2099874?seq=1#page_scan_tab_contents
    
    1. Normalize weights to sum to 1
    2. Sum(weight * element^r)^1/r
    3. Use large R for max, small R for min
    */
    val totalSignificance = applicabilitySignificance.map(_._2).sum
    if (totalSignificance == 0) {
      // Dunno.
      return 0.0
    }
    
    val elements  = applicabilitySignificance.map(p => Math.pow(p._1, tension) * p._2 / totalSignificance)
    val output    = Math.pow(elements.sum, 1.0/tension)
    output
  }
}
