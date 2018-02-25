package Strategery.Strategies.Zerg.ZvP

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.TwoHatchHydra
import Strategery.Strategies.Strategy
import bwapi.Race

object TwoHatchHydra extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TwoHatchHydra) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
