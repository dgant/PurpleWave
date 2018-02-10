package Strategery.Strategies.Zerg.ZvE

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.NinePoolMuta
import Strategery.Strategies.Strategy
import bwapi.Race

object NinePoolMuta extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = { Some(new NinePoolMuta) }
}
