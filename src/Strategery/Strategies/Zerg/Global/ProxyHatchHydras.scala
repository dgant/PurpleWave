package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.ProxyHatch
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyHatchHydras extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def buildGameplan(): Option[Plan] = Some(new ProxyHatch)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown, Race.Terran, Race.Protoss)
}
