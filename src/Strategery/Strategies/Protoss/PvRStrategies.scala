package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.PvR.{PvR2Gate4Gate, PvRForgeDT}
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Seq[Race] = Vector(Race.Unknown)
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
object PvROpen2Gate910 extends PvR2GateStrategy
object PvROpen2Gate1012 extends PvR2GateStrategy
object PvRProxy2Gate extends PvR2GateStrategy {
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool)
}

object PvR2Gate4Gate extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvR2Gate4Gate)
}

object PvR1BaseDT extends PvRStrategy {
  override def allowedVsHuman: Boolean = false
  override def gameplan: Option[Plan] = Some(new PvRForgeDT)
}

