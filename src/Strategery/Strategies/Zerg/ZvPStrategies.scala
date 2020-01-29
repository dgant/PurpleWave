package Strategery.Strategies.Zerg

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.{ZvP2HatchMuta}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZvP2HatchMuta extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP2HatchMuta) }
}

/// New stuff

abstract class ZvPOpening extends ZvPStrategy {
  override val choices = Seq(Seq(ZvPReactiveBust, ZvPZerglingBust, ZvPHydraBust, ZvPMutaliskBust, ZvPNeverBust))
}

object ZvP12Hatch extends ZvPOpening
object ZvPOverpool extends ZvPOpening
object ZvP9Pool extends ZvPOpening {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.proxyGateway,
    With.fingerprints.nexusFirst,
    With.fingerprints.workerRush)
}

object ZvPReactiveBust extends ZvPStrategy
object ZvPHydraBust extends ZvPStrategy
object ZvPZerglingBust extends ZvPStrategy
object ZvPMutaliskBust extends ZvPStrategy
object ZvPNeverBust extends ZvPStrategy