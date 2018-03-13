package Strategery.Strategies.Protoss

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class PvPOpening extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvPLateGameCarrier, PvPLateGameGateway))
}

object PvPOpen1GateReaverExpand   extends PvPOpening
object PvPOpen2Gate1012           extends PvPOpening
object PvPOpen2GateDTExpand       extends PvPOpening
object PvPOpen2GateRobo           extends PvPOpening
object PvPOpen3GateSpeedlots      extends PvPOpening
object PvPOpen4GateGoon           extends PvPOpening
object PvPOpen12Nexus5Zealot      extends PvPOpening
object PvPOpen1015GateDTs         extends PvPOpening
object PvPOpenProxy2Gate          extends PvPOpening {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvpOpenersTransitioningFrom2Gate)
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvPLateGameCarrier extends Strategy
object PvPLateGameGateway extends Strategy