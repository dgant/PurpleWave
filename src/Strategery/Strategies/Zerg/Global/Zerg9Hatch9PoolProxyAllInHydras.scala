package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.Zerg9Hatch9PoolProxyHydras
import Strategery.Strategies.Strategy
import bwapi.Race

object Zerg9Hatch9PoolProxyAllInHydras extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def buildGameplan(): Option[Plan] = Some(new Zerg9Hatch9PoolProxyHydras)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss, Race.Random)
}
