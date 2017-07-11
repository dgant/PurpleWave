package Strategery.Strategies.Options.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.ProbeRush
import Strategery.Strategies.Strategy

object WorkerRush3StartLocations extends Strategy {
  
  override lazy val buildGameplan: Option[Plan] = Some(new ProbeRush)
  
  override def startLocationsMin = 3
}
