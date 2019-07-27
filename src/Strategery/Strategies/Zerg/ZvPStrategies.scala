package Strategery.Strategies.Zerg

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.{ZvP2HatchMuta, ZvP3Hatch, ZvP6Hatch}
import Strategery.Strategies.Strategy
import Strategery.{Destination, Heartbreak, StarCraftMap}
import bwapi.Race

abstract class ZvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
object ZvP3Hatch extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP3Hatch) }
}
object ZvP6Hatch extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP6Hatch) }
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination, Heartbreak)
}
object ZvP2HatchMuta extends ZvPStrategy {
  override def gameplan: Option[Plan] = { Some(new ZvP2HatchMuta) }
}



/// New stuff

abstract class ZvPOpening extends ZvPStrategy {
  override val choices = Seq(Seq(ZvPStandard, ZvPBust))
}

object ZvP12Hatch extends ZvPOpening
object ZvPOverpool extends ZvPOpening
object ZvP9Pool extends ZvPOpening {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.proxyGateway,
    With.fingerprints.nexusFirst,
    With.fingerprints.workerRush)
}

object ZvPStandard extends ZvPStrategy {

}
object ZvPBust extends ZvPStrategy