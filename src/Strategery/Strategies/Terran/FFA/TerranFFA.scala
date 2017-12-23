package Strategery.Strategies.Terran.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Terran.FFA.TerranFFA
import Strategery.Strategies.Strategy
import bwapi.Race

object TerranFFA extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new TerranFFA) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def ffa = true
}
