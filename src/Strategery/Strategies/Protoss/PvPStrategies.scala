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
  // This blacklist essentially bans it, at time of writing
  blacklistVs(With.fingerprints.twoGate)
}
object PvP5Zealot extends PvPStrategy
object PvPGateCore extends PvPStrategy

////////////////////////
// Main continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  addChoice(PvPGateCore)
  addChoice(PvPObs, PvPReaver)
  // No restrictions; Preserve this as our "Always valid" choice
}
object PvPReaver extends PvPStrategy
object PvPObs extends PvPStrategy {
  whitelistVs(With.fingerprints.dtRush) // Until we make better use of the Obs
}
object PvPDT extends PvPStrategy {
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.robo)
}
object PvPCoreExpand extends PvPStrategy {
  setRushTilesMinimum(200) // Maybe extend if ramp is high ground
  setMinimumGamesVsOpponent(3)
  addChoice(PvPGateCore)
}
object PvP3GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.dtRush)
  blacklistOn(MapGroups.badForMassGoon: _*)
}
object PvP4GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
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

