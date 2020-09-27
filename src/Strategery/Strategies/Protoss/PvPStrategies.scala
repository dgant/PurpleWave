package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

object PvP3rdBaseFast extends PvPStrategy
object PvP3rdBaseSlow extends PvPStrategy

abstract class PvPOpening extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvP3rdBaseSlow, PvP3rdBaseFast))
}
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
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.twoGate)
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
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)
  override def minimumGamesVsOpponent: Int = 1
}
