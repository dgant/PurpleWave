package Strategery.Strategies.Options.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Proxy2GateAtEnemyNatural
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy2Gate2StartLocations extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new Proxy2GateAtEnemyNatural)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def startLocationsMin = 2
  override def startLocationsMax = 2
}
