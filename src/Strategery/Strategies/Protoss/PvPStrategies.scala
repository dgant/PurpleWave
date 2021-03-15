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
object PvPRobo1012 extends PvPStrategy {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.nexusFirst) // TODO: Also NZCore
  override def entranceRamped: Boolean = false
  override def entranceFlat: Boolean = false
}
object PvPRobo1Gate extends PvPStrategy
object PvPRobo extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = super.choices ++ Seq(Seq(PvPRobo1012, PvPRobo1Gate))
}
object PvP2GateDTExpand extends PvPStrategy
object PvP2Gate1012Goon extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.earlyForge, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
}
object PvP2Gate1012DT extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.earlyForge, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.robo)
}
object PvP2GateGoon extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.dtRush, With.fingerprints.oneGateCore, With.fingerprints.fourGateGoon)
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
}
object PvP3GateGoon extends PvPStrategy {
  //override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.twoGate, With.fingerprints.fourGateGoon)
  //override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  //override def minimumGamesVsOpponent: Int = 2 -- Disabled AIST4 because strategy picker refused to pick it
}
object PvP4GateGoon extends PvPStrategy {
  //override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  //override def minimumGamesVsOpponent: Int = 2
  //override def entranceRamped: Boolean = false
}
object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
}
object PvP1ZealotExpand extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)
  override def minimumGamesVsOpponent: Int = 1
}
