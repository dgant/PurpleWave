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

object PvP3Zealot extends PvPStrategy {
  // The value is arriving with 3 zealots vs fewer than 3 combat units, which ideally is NZCore but is probably okay vs coreZ too
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.coreBeforeZ)
  // Longer maps
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Arcadia, Heartbreak, Aztec, MatchPoint, TauCross)
  override def rushDistanceMaximum: Double = 200
}
object PvP5Zealot extends PvPStrategy {
  // No blacklisting: Preserve this option against someone who insists on proxying
}
object PvP1012 extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP3Zealot, PvP5Zealot))
  // TODO: Only enable vs appropriate builds, eg almost anything but ZCore, CoreZ, or ZCoreZ
  override def responsesWhitelisted: Iterable[Fingerprint] = Iterable(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate99)
}
object PvPGateCoreRange extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Python)
  override def entranceInverted: Boolean = false
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.proxyGateway, With.fingerprints.twoGate99)
}
object PvPGateCoreGate extends PvPStrategy {
  // No blacklisting; Preserve this as our "Always valid" choice
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
  // No blacklisting; Preserve this as our "Always valid" choice
}
object PvPDT extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.robo, With.fingerprints.forgeFe)
}
object PvPCoreExpand extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate))
  override def rushDistanceMinimum: Double = 200 // Maybe extend if ramp is high ground
}
object PvP3GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def minimumGamesVsOpponent: Int = 2
}
object PvP4GateGoon extends PvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange))
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.dtRush)
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Aztec, CircuitBreaker, Destination, Roadrunner, MatchPoint)
  override def minimumGamesVsOpponent: Int = 2
}

//////////////
// Oddballs //
//////////////

object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Iterable(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
}

