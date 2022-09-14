package Strategery.Strategies.Protoss

import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery._
import bwapi.Race

abstract class PvPStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Protoss)
}

///////////////////
// Opening steps //
///////////////////

object PvP1012 extends PvPStrategy {
  addChoice(PvP3Zealot, PvP5Zealot)
  whitelistVs(
    With.fingerprints.nexusFirst,
    With.fingerprints.proxyGateway,
    With.fingerprints.twoGate99)
}

object PvP3Zealot extends PvPStrategy {
  // You're advantaged when arriving with 3 zealots vs fewer than 3 combat units, which ideally is NZCore but is probably okay vs coreZ too
  whitelistVs(With.fingerprints.coreBeforeZ)
}
object PvP5Zealot extends PvPStrategy
object PvPGateCoreRange extends PvPStrategy {
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPGateCoreGate extends PvPStrategy {
  whitelistVs(
    With.fingerprints.proxyGateway,
    With.fingerprints.twoGate99,
    With.fingerprints.twoGate)
}
object PvPGateCoreTech extends PvPStrategy {
  setEntranceInverted(false)
  setEntranceFlat(false)
}
object PvP1015 extends PvPStrategy {
  setEntranceRamped(false)
  blacklistVs(
    With.fingerprints.twoGate99,
    With.fingerprints.proxyGateway,
    With.fingerprints.mannerPylon)
  blacklistOn(MapGroups.badForMassGoon: _*)
}

////////////////////////
// Main continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  addChoice(PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech, PvP1015)
  addChoice(PvPObs, PvPReaver)
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPReaver extends PvPStrategy
object PvPObs extends PvPStrategy
object PvPDT extends PvPStrategy {
  addChoice(PvP1012, PvPGateCoreRange, PvPGateCoreGate, PvPGateCoreTech, PvP1015)
  blacklistVs(With.fingerprints.robo)
}
object PvPCoreExpand extends PvPStrategy {
  setRushTilesMinimum(200) // Maybe extend if ramp is high ground
  setMinimumGamesVsOpponent(1)
  addChoice(PvPGateCoreRange, PvPGateCoreGate, PvP1015)
}
object PvP3GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCoreRange, PvPGateCoreGate)
  blacklistVs(With.fingerprints.dtRush)
  blacklistOn(MapGroups.badForMassGoon: _*)
}
object PvP4GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCoreRange, PvPGateCoreGate)
  blacklistVs(With.fingerprints.dtRush)
  blacklistOn(MapGroups.badForMassGoon: _*)
}

//////////////
// Oddballs //
//////////////

object PvPProxy2Gate extends PvPStrategy {
  blacklistOn(MapGroups.badForProxying: _*)
  whitelistVs(
    With.fingerprints.nexusFirst,
    With.fingerprints.coreBeforeZ)
  blacklistVs(
    With.fingerprints.twoGate,
    With.fingerprints.proxyGateway,
    With.fingerprints.forgeFe,
    With.fingerprints.earlyForge)
}

