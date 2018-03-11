package Strategery.Strategies.Protoss.PvT
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
abstract class PvTOpener extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT3BaseCarrier,
    PvT3BaseArbiter
  ))
}
object PvT13Nexus extends PvTOpener
object PvT21Nexus extends PvTOpener
object PvTDTExpand extends PvTOpener
object PvTEarly4Gate extends PvTOpener
object PvT2BaseCarrier extends PvTStrategy
object PvT3BaseCarrier extends PvTStrategy
object PvT2BaseArbiter extends PvTStrategy
object PvT3BaseArbiter extends PvTStrategy