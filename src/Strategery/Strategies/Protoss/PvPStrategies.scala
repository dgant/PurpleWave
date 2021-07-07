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
object PvP3Zealot extends PvPStrategy
object PvP5Zealot extends PvPStrategy
object PvP1012 extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP3Zealot, PvP5Zealot))
}
object PvPGateCoreTech extends PvPStrategy
object PvPGateCoreGate extends PvPStrategy
object PvPRobo extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreTech, PvPGateCoreGate))
}
object PvP2GateDTExpand extends PvPStrategy
object PvP3GateGoon extends PvPStrategy {
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.twoGate, With.fingerprints.fourGateGoon)
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 2
}
object PvP4GateGoon extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 2
  override def entranceRamped: Boolean = false
}
object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
}
object PvP1ZealotExpand extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)
  override def minimumGamesVsOpponent: Int = 1
}
