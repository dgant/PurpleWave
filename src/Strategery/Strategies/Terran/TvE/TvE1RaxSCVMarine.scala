package Strategery.Strategies.Terran.TvE

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvE1RaxSCVMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvE1RaxSCVMarine extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE1RaxSCVMarine) }
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss, Race.Unknown)

  override def allowedVsHuman: Boolean = false

  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(
    With.fingerprints.forgeFe,
    With.fingerprints.fourPool,
    With.fingerprints.twoGate,
    With.fingerprints.bbs,
    With.fingerprints.twoRax1113,
  )
}
