package Strategery.Strategies.Protoss

import Lifecycle.With
import Planning.MacroFacts
import ProxyBwapi.Races.Protoss
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

object CanGateCoreOnThisMap {
  val criticalRushDistance = 150
  def apply(): Boolean = With.strategy.rushDistanceMin > criticalRushDistance
}

object PvPGateCore extends PvPStrategy {
  addSelectionRequirement(() => CanGateCoreOnThisMap())
}

object PvP1012 extends PvPStrategy {
  addChoice(PvP3Zealot, PvP5Zealot)
}

object PvPNexusFirst extends PvPStrategy {
  whitelistVs(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)
  blacklistVs(With.fingerprints.gatewayFirst)
  setMinimumGamesVsOpponent(3)
}

object PvP3Zealot extends PvPStrategy {
  // You're advantaged when arriving with 3 zealots vs fewer than 3 combat units, which ideally is NZCore but is probably okay vs coreZ too
  whitelistVs(With.fingerprints.coreBeforeZ)
  // This blacklist essentially bans it, at time of writing
  blacklistVs(With.fingerprints.twoGate)
}
object PvP5Zealot extends PvPStrategy

////////////////////////
// Main continuations //
////////////////////////

object PvPRobo extends PvPStrategy {
  addChoice(PvPGateCore)
  addChoice(PvPObs, PvPReaver)
  // No restrictions; Preserve this as our "Always valid" choice
  addActivationRequirement(() => haveEver(Protoss.RoboticsFacility))
}
object PvPReaver extends PvPStrategy {
  addActivationRequirement(() => haveEver(Protoss.Reaver))
}
object PvPObs extends PvPStrategy {
  whitelistVs(With.fingerprints.dtRush) // Until we make better use of the Obs
  addActivationRequirement(() => haveEver(Protoss.Observatory))
}
object PvPDT extends PvPStrategy {
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.robo)
  addActivationRequirement(() => haveEver(Protoss.CitadelOfAdun))
}
object PvPCoreExpand extends PvPStrategy {
  setMinimumGamesVsOpponent(3)
  addChoice(PvPGateCore)
  addSelectionRequirement(() =>
    rushTilesMinimum >= 200
    || MapGroups.strongNatural.exists(_())
    ||   MacroFacts.enemyRecentStrategy(With.fingerprints.robo, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
    || ! MacroFacts.enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))
  addActivationRequirement(() => unitsEver(Protoss.Nexus) >= 2)
}
object PvP3GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.dtRush)
  addSelectionRequirement(() => entranceInverted || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
  addActivationRequirement(() => unitsEver(Protoss.Gateway) >= 3)
}
object PvP4GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.dtRush)
  addSelectionRequirement(() => entranceInverted || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
  addSelectionRequirement(() => entranceInverted || ! With.fingerprints.fourGateGoon.recently)
  addActivationRequirement(() => unitsEver(Protoss.Gateway) >= 4)
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

