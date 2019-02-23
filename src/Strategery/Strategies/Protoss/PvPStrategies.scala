package Strategery.Strategies.Protoss

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

abstract class PvPOpening extends PvPStrategy

object PvP1GateReaverExpand extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.nexusFirst, With.fingerprints.fourGateGoon)
  override def entranceInverted: Boolean = false
  override def entranceFlat: Boolean = false
}

object PvP3GateRobo extends PvPOpening {
  override def entranceInverted: Boolean = false
}

object PvPGateGateRobo extends PvPOpening {
  override def entranceRamped: Boolean = false
  override def entranceFlat: Boolean = false
}

object PvP2Gate1012 extends PvPOpening

object PvP2Gate1012Goon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.forgeFe)
  //override def rushDistanceMaximum: Int = 5000
}

object PvP2GateDTExpand extends PvPOpening {
  //override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.proxyGateway, With.fingerprints.robo)
}

object PvP3GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush, With.fingerprints.proxyGateway)
}
object PvP4GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def entranceRamped: Boolean = false
}
object PvPProxy2Gate extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe)
}