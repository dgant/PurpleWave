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
object PvP2Gate1012 extends PvPOpening
object PvP2Gate1012Goon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.forgeFe)
}
object PvP2Gate1012DT extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.forgeFe, With.fingerprints.robo)
}
object PvP2GateDTExpand extends PvPOpening
object PvP2GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = PvP3GateGoon.responsesBlacklisted
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.twoGate)
}
object PvP3GateGoon extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
}
object PvP3GateGoonCounter extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = PvP3GateGoon.mapsBlacklisted
  override def responsesBlacklisted: Iterable[Fingerprint] = PvP3GateGoon.responsesBlacklisted
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.forgeFe, With.fingerprints.robo)
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
object PvP1ZealotExpand extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.dtRush)
  override def startLocationsMin: Int = 3
}
object PvPRobo extends PvPOpening