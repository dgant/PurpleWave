package Strategery.Strategies.Protoss

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTReaverCarrierCheese
import Strategery.{BlueStorm, Hitchhiker, MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
abstract class PvTBasicOpener extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseArbiter,
    PvT3BaseArbiter,
    PvT2BaseCarrier,
    PvT3BaseCarrier
  ))
}
object PvT13Nexus extends PvTBasicOpener
object PvT21Nexus extends PvTBasicOpener
object PvT1015Expand extends PvTBasicOpener
object PvT2GateObserver extends PvTBasicOpener
object PvTDTExpand extends PvTBasicOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseArbiter,
    PvT3BaseArbiter,
    PvT3BaseCarrier
  ))
}
object PvTEarly1015GateGoonDT extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT3BaseArbiter,
      PvT3BaseCarrier))
}
object PvT2BaseCarrier extends PvTStrategy  { override val mapsBlacklisted = Iterable(BlueStorm) }
object PvT3BaseCarrier extends PvTStrategy { override val mapsBlacklisted = MapGroups.badForFastThirdBases }
object PvT2BaseArbiter extends PvTStrategy { override val mapsBlacklisted = Iterable(BlueStorm) }
object PvT3BaseArbiter extends PvTStrategy { override val mapsBlacklisted = MapGroups.badForFastThirdBases }

object PvTEarly1GateStargateTemplar extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(PvT2BaseArbiter))
}

object PvTProxy2Gate extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvtOpenersTransitioningFrom2Gate)
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvTReaverCarrierCheese extends PvTStrategy {
  override def gameplan: Option[Plan] = Some(new PvTReaverCarrierCheese)
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Vector("Rooijackers", "Leta"))
}