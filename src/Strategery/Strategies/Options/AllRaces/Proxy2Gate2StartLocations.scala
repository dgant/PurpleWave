package Strategery.Strategies.Options.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Proxy2GateAtNatural
import Strategery.Strategies.Strategy

object Proxy2Gate2StartLocations extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new Proxy2GateAtNatural)
  
  override def startLocationsMin = 2
  override def startLocationsMax = 2
}
