package Strategery.Strategies.Zerg

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.Gameplans.Zerg.ZvE.{ZvE4Pool, ZvE9Pool2HatchSpeed}
import Strategery.{Benzene, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class ZergStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Seq(Race.Zerg)
}

object ZvE4Pool extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvE4Pool)
  override def mapsBlacklisted: Seq[StarCraftMap] = Seq(Benzene)

  override def responsesBlacklisted: Seq[Fingerprint] = Seq(
    With.fingerprints.ninePool,
    With.fingerprints.overpool,
  )
}

object ZvE9Pool2HatchSpeed extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvE9Pool2HatchSpeed)

  override def responsesBlacklisted: Seq[Fingerprint] = Seq(
    With.fingerprints.wallIn,
    With.fingerprints.twoGate,
    With.fingerprints.tenHatch,
  )
}