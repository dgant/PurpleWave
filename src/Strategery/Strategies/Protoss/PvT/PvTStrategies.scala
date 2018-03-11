package Strategery.Strategies.Protoss.PvT
import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
abstract class PvTBasicOpener extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT3BaseCarrier,
    PvT3BaseArbiter
  ))
}
object PvT13Nexus extends PvTBasicOpener
object PvT21Nexus extends PvTBasicOpener
object PvTDTExpand extends PvTBasicOpener
object PvTEarly4Gate extends PvTBasicOpener
object PvT2BaseCarrier extends PvTStrategy
object PvT3BaseCarrier extends PvTStrategy { override val prohibitedMaps = MapGroups.badForFastThirdBases }
object PvT2BaseArbiter extends PvTStrategy
object PvT3BaseArbiter extends PvTStrategy { override val prohibitedMaps = MapGroups.badForFastThirdBases }

object PvTEarly1015GateGoonDT extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT3BaseArbiter))
}
object PvTEarly1GateStargateTemplar extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(PvT2BaseArbiter))
}

object PvTProxy2Gate extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvtOpenersTransitioningFrom2Gate)
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
