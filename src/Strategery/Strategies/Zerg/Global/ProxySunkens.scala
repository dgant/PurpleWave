package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.Zerg9PoolProxySunkens
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxySunkens extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = Some(new Zerg9PoolProxySunkens)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}
