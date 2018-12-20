package Strategery.Strategies.Protoss

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.{BlueStorm, Hitchhiker, MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

abstract class PvPOpening extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(
    PvPLateGameCarrier,
    PvPLateGameArbiter))
}

abstract class PvPOpeningIntoCarriers extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(
    PvPLateGameCarrier,
    PvPLateGameArbiter,
    PvPLateGame2BaseReaverCarrier_SpecificOpponents,
    PvPLateGame2BaseReaverCarrier_SpecificMaps))
}

object PvP1GateReaverExpand extends PvPOpeningIntoCarriers {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.nexusFirst, With.fingerprints.fourGateGoon)
  override def entranceInverted: Boolean = false
  override def entranceFlat: Boolean = false
}

object PvP3GateRobo extends PvPOpeningIntoCarriers {
  override def entranceInverted: Boolean = false
}

object PvP2GateRobo extends PvPOpeningIntoCarriers

object PvP2Gate1012 extends PvPOpeningIntoCarriers

object PvP2Gate1012Goon extends PvPOpeningIntoCarriers {
  //override def rushDistanceMaximum: Int = 5000
}

object PvP2GateDTExpand extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.proxyGateway, With.fingerprints.robo)
}

object PvP3GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.dtRush, With.fingerprints.proxyGateway)
}
object PvP4GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def entranceRamped: Boolean = false
}
object PvPProxy2Gate extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate)
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