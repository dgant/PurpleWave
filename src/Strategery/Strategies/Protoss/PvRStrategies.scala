package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvR.{PvR2Gate4Gate, PvRForgeDT, PvRTinfoil}
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}

abstract class PvR1GateCoreStrategy extends PvRStrategy {
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom1GateCore,
    ProtossChoices.pvpOpenersTransitioningFrom1GateCore,
    ProtossChoices.pvzOpenersTransitioningFrom1GateCore)
}

abstract class PvR2GateStrategy extends PvRStrategy {
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom2Gate,
    ProtossChoices.pvpOpenersTransitioningFrom2Gate,
    ProtossChoices.pvzOpenersTransitioningFrom2Gate)
}

object PvROpenZCoreZ extends PvR1GateCoreStrategy
object PvROpenZZCore extends PvR1GateCoreStrategy
object PvROpen2Gate910 extends PvR2GateStrategy
object PvROpen2Gate1012 extends PvR2GateStrategy
object PvRDT extends PvRStrategy {
  override lazy val choices = Vector(
    Seq(PvTDTExpand),
    Seq(PvP2GateDTExpand),
    Seq(PvZMidgameBisu))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
}
object PvRProxy2Gate extends PvR2GateStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
}

object PvR2Gate4Gate extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvR2Gate4Gate)
}
abstract class PvRTinfoils extends PvRStrategy {
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Iterable(
    "Kurdiumov",
    "Churchill",
    "UAlbertaBot",
    "OpprimoBot"
  ))
}

object PvRTinfoil2018 extends PvRTinfoils {
  override def gameplan: Option[Plan] = Some(new PvRTinfoil)
}

object PvR1BaseDT extends PvRTinfoils {
  override def gameplan: Option[Plan] = Some(new PvRForgeDT)
}

