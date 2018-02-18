package Strategery.Strategies.Zerg.ZvE

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.OneHatchLurker
import Strategery.Strategies.Strategy
import bwapi.Race

object OneHatchLurker extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new OneHatchLurker) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
