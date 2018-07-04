package Strategery.Strategies.Protoss

import Strategery.{BlueStorm, MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

class PvPOpening extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvPLateGameCarrier, PvPLateGameArbiter))
}

object PvPOpen1GateReaverExpand   extends PvPOpening
object PvPOpen2Gate1012           extends PvPOpening
object PvPOpen2GateDTExpand       extends PvPOpening
object PvPOpen2GateRobo           extends PvPOpening
object PvPOpen4GateGoon           extends PvPOpening
object PvPOpenProxy2Gate          extends PvPOpening {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvpOpenersTransitioningFrom2Gate)
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvPLateGameCarrier extends PvPStrategy
object PvPLateGameArbiter extends PvPStrategy {
  override def prohibitedMaps: Iterable[StarCraftMap] = Iterable(BlueStorm)
}