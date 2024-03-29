package Strategery.Strategies.Zerg

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.ZvP2HatchMuta
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvPStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Seq(Race.Zerg)
  override def enemyRaces: Seq[Race] = Seq(Race.Protoss)
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
  override def responsesWhitelisted: Seq[Fingerprint] = Seq(
    With.fingerprints.proxyGateway,
    With.fingerprints.nexusFirst,
    With.fingerprints.workerRush)
}

object ZvPReactiveBust extends ZvPStrategy
object ZvPHydraBust extends ZvPStrategy
object ZvPZerglingBust extends ZvPStrategy
object ZvPMutaliskBust extends ZvPStrategy
object ZvPNeverBust extends ZvPStrategy