package Strategery.Strategies.Terran.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Terran.FFA.TerranFFABio
import Strategery.Strategies.Strategy
import bwapi.Race

object TerranFFABio extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new TerranFFABio) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def ffa = true
}
