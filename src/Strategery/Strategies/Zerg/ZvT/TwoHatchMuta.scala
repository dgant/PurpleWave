package Strategery.Strategies.Zerg.ZvT

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvT.TwoHatchMuta
import Strategery.Strategies.Strategy
import bwapi.Race

object TwoHatchMuta extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = { Some(new TwoHatchMuta) }
}
