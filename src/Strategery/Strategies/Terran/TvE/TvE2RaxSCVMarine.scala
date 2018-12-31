package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvE2RaxSCVMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvE2RaxSCVMarine extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE2RaxSCVMarine) }
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
