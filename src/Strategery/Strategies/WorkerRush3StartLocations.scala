package Strategery.Strategies

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.ProbeRush

object WorkerRush3StartLocations extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new ProbeRush)
  
  override def startLocationsMin = 3
}
