package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery._
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  override def ourRaces   : Seq[Race]  = Seq(Race.Protoss)
  override def enemyRaces : Seq[Race]  = Seq(Race.Protoss)
}

///////////////////
// Opening steps //
///////////////////

object PvP1012 extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvP3Zealot, PvP5Zealot))
  override def responsesWhitelisted: Seq[Fingerprint] = Seq(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate99)
}

object PvP3Zealot extends PvPStrategy {
  // You're advantaged when arriving with 3 zealots vs fewer than 3 combat units, which ideally is NZCore but is probably okay vs coreZ too
  override def responsesWhitelisted: Seq[Fingerprint] = Seq(With.fingerprints.coreBeforeZ)
}
object PvP5Zealot extends PvPStrategy {
}
object PvPGateCoreRange extends PvPStrategy {
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPGateCoreGate extends PvPStrategy {
  override def responsesWhitelisted: Seq[Fingerprint] = Seq(With.fingerprints.proxyGateway, With.fingerprints.twoGate99, With.fingerprints.twoGate)
}
object PvPGateCoreTech extends PvPStrategy {
  override def entranceInverted: Boolean = false
  override def entranceFlat: Boolean = false
}
object PvP1015 extends PvPStrategy {
  override def entranceRamped: Boolean = false
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.twoGate99, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon)
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForMassGoon
}

////////////////////////
// Core continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech, PvP1015))
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPDT extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech, PvP1015))
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.robo)
}
object PvPCoreExpand extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvPGateCoreRange, PvPGateCoreGate, PvP1015))
  override def rushTilesMinimum: Int = 200 // Maybe extend if ramp is high ground
  override def minimumGamesVsOpponent: Int = 1
}
object PvP3GateGoon extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate))
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.dtRush)
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForMassGoon
  override def minimumGamesVsOpponent: Int = 1
}
object PvP4GateGoon extends PvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvP1012, PvPGateCoreRange, PvPGateCoreGate))
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.dtRush)
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForMassGoon
  override def minimumGamesVsOpponent: Int = 1
}

//////////////
// Oddballs //
//////////////

object PvPProxy2Gate extends PvPStrategy {
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.forgeFe, With.fingerprints.earlyForge)
  override def responsesWhitelisted: Seq[Fingerprint] = Seq(With.fingerprints.nexusFirst, With.fingerprints.coreBeforeZ)
}

