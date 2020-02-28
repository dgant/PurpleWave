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
object PvPRobo extends PvPOpening
object PvP2GateDTExpand extends PvPOpening
object PvP2Gate1012Goon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.earlyForge, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
}
object PvP2Gate1012DT extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.earlyForge, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.robo)
}
object PvP2GateGoon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.dtRush, With.fingerprints.oneGateCore)
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.twoGate)
}
object PvP3GateGoon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 2
}
object PvP4GateGoon extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 2
  override def entranceRamped: Boolean = false
}
object PvPProxy2Gate extends PvPOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
}
object PvP1ZealotExpand extends PvPOpening {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway)
  override def minimumGamesVsOpponent: Int = 1
}
