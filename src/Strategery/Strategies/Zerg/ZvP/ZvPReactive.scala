package Strategery.Strategies.Zerg.ZvP

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPReactive
import Strategery.Strategies.Strategy
import bwapi.Race

object ZvPReactive extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def gameplan: Option[Plan] = { Some(new ZvPReactive) }
}
