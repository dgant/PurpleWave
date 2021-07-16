package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, Python, StarCraftMap}
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

///////////////////
// Opening steps //
///////////////////

object PvP3Zealot extends PvPStrategy
object PvP5Zealot extends PvPStrategy
object PvP1012 extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP3Zealot, PvP5Zealot))
}
object PvPGateCoreTech extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Python)
}
object PvPGateCoreGate extends PvPStrategy

////////////////////////
// Core continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreTech, PvPGateCoreGate))
}
object PvPDT extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreTech))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo)
}
object PvP3GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreTech, PvPGateCoreGate))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 5
  override def entranceRamped = false
}
object PvP4GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreTech))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 5
  override def entranceRamped: Boolean = false
}

//////////////
// Oddballs //
//////////////

object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
}
object PvP1ZealotExpand extends PvPStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.cannonRush)
  override def minimumGamesVsOpponent: Int = 1
}
