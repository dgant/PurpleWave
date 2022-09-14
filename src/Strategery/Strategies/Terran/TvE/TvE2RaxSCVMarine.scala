package Strategery.Strategies.Terran.TvE

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvE.TvE2RaxSCVMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvE2RaxSCVMarine extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE2RaxSCVMarine) }
  override def ourRaces: Seq[Race] = Seq(Race.Terran, Race.Unknown)

  override def responsesBlacklisted: Seq[Fingerprint] = Seq(
    With.fingerprints.forgeFe,
    With.fingerprints.fourPool
  )
}
