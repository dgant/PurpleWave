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
object PvP2Gate1012GoonCounter extends PvPOpening {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.forgeFe, With.fingerprints.robo)
}
object PvP2Gate1012DT extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.forgeFe, With.fingerprints.robo)
}

object PvP2GateDTExpand extends PvPOpening {
  //override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.proxyGateway)
}
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

// TODO -- unfinished

object PvP1ZealotExpand extends PvPOpening {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.robo)
}
object PvPObsReaverExpand extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
}
object PvPReaverExpand extends PvPOpening {
}
object PvPReaverPush extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
}
object PvPObsReaverPush extends PvPOpening {

}