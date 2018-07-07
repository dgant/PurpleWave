package Strategery.Strategies.Protoss

import Strategery.{BlueStorm, Hitchhiker, MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

class PvPOpening extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(
    PvPLateGameCarrier,
    PvPLateGameArbiter,
    PvPLateGame2BaseReaverCarrier_SpecificOpponents,
    PvPLateGame2BaseReaverCarrier_SpecificMaps))
}

object PvPOpen1GateReaverExpand   extends PvPOpening
object PvPOpen2Gate1012           extends PvPOpening
object PvPOpen2GateDTExpand       extends PvPOpening
object PvPOpen2GateRobo           extends PvPOpening
object PvPOpen4GateGoon           extends PvPOpening
object PvPOpenProxy2Gate          extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvPLateGameCarrier extends PvPStrategy
object PvPLateGameArbiter extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Iterable(BlueStorm)
}
object PvPLateGame2BaseReaverCarrier_SpecificOpponents extends PvPStrategy {
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Vector("McRave"))
}
object PvPLateGame2BaseReaverCarrier_SpecificMaps extends PvPStrategy {
  override def mapsWhitelisted: Option[Iterable[StarCraftMap]] = Some(Vector(BlueStorm, Hitchhiker))
}