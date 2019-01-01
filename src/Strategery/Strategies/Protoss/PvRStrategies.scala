package Strategery.Strategies.Protoss

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvR.PvRTinfoil
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
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
object PvROpenProxy2Gate extends PvR2GateStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvROpenTinfoil extends PvR2GateStrategy {
  override def gameplan: Option[Plan] = Some(new PvRTinfoil)
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Iterable(
    "Kurdiumov",
    "Churchill",
    "UAlbertaBot",
    "OpprimoBot"
  ))
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Unknown)
}

