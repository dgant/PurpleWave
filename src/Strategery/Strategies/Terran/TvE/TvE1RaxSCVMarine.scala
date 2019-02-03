package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvE1RaxSCVMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvE1RaxSCVMarine extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE1RaxSCVMarine) }
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss, Race.Unknown)
}
