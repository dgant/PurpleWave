package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery._
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
}

///////////////////
// Opening steps //
///////////////////

object PvP1012 extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP3Zealot, PvP5Zealot))
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate99)
}

object PvP3Zealot extends PvPStrategy {
  // You're advantaged when arriving with 3 zealots vs fewer than 3 combat units, which ideally is NZCore but is probably okay vs coreZ too
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.coreBeforeZ)
}
object PvP5Zealot extends PvPStrategy {
}
object PvPGateCoreRange extends PvPStrategy {
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPGateCoreGate extends PvPStrategy {
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.proxyGateway, With.fingerprints.twoGate99, With.fingerprints.twoGate)
}
object PvPGateCoreTech extends PvPStrategy {
  override def entranceInverted: Boolean = false
  override def entranceFlat: Boolean = false
}

////////////////////////
// Core continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech))
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPDT extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo)
}
object PvPCoreExpand extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate))
  override def rushDistanceMinimum: Double = 200 // Maybe extend if ramp is high ground
  override def minimumGamesVsOpponent: Int = 1
}
object PvP3GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 1
}
object PvP4GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Aztec, CircuitBreaker, Destination, Roadrunner, MatchPoint)
  override def minimumGamesVsOpponent: Int = 1
}

//////////////
// Oddballs //
//////////////

object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.nexusFirst, With.fingerprints.coreBeforeZ)
}

