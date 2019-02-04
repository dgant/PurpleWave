package Strategery.Strategies.Terran.TvE

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvEProxy5Rax
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy5Rax extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new TvEProxy5Rax)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def mapsBlacklisted: Vector[StarCraftMap] = MapGroups.badForProxying

  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(
    With.fingerprints.fourPool
  )
}
