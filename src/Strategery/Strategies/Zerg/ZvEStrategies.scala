package Strategery.Strategies.Zerg

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{ZvE4Pool, ZvE9Pool2HatchSpeed, ZvESparkle}
import Strategery.{Benzene, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class ZergStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
}

object ZvE4Pool extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvE4Pool)
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Benzene)

  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(
    With.fingerprints.ninePool,
    With.fingerprints.overpool,
  )
}

object ZvE9Pool2HatchSpeed extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvE9Pool2HatchSpeed)

  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(
    With.fingerprints.wallIn,
    With.fingerprints.twoGate,
    With.fingerprints.tenHatch,
  )
}

object ZvESparkle extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvESparkle)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}