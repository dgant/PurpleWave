package Strategery.Strategies.Protoss

import Lifecycle.With
import Macro.Facts.MacroFacts
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Personalities.PersonalityCheese
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
  addChoice(PvPRobo, PvPDT, PvPCoreExpand, PvP3GateGoon, PvP4GateGoon)
  addActivationRequirement(() => units(Protoss.CyberneticsCore) >= 1 && units(Protoss.Gateway) < 2 && units(Protoss.Nexus) < 2)
}

object PvP1012 extends PvPStrategy {
  addChoice(PvP3Zealot, PvP5Zealot)
  addChoice(PvPDT, PvP3GateGoon, PvP4GateGoon)
}

object PvPNexusFirst extends PvPStrategy {
  whitelistVs(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)
  blacklistVs(With.fingerprints.gatewayFirst)
  setMinimumGamesVsOpponent(5)
  addActivationRequirement(() => units(Protoss.Nexus) >= 2 && ! haveEver(Protoss.Gateway, Protoss.Forge))
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
  addChoice(PvPObs, PvPReaver)
  // No restrictions; Preserve this as our "Always valid" choice
  addActivationRequirement(() => have(Protoss.RoboticsFacility) && unitsEver(Protoss.Nexus) < 2)
  banPersonality(PersonalityCheese)
}
object PvPReaver extends PvPStrategy {
  addActivationRequirement(() => have(Protoss.Shuttle, Protoss.RoboticsSupportBay) && ! haveEver(Protoss.Observatory) && unitsEver(Protoss.Nexus) < 2)
}
object PvPObs extends PvPStrategy {
  whitelistVs(With.fingerprints.dtRush) // Until we make better use of the Obs
  addActivationRequirement(() => have(Protoss.Observatory) && ! haveEver(Protoss.Shuttle, Protoss.RoboticsSupportBay) && unitsEver(Protoss.Nexus) < 2)
}
object PvPDT extends PvPStrategy {
  blacklistVs(With.fingerprints.robo)
  addActivationRequirement(() => have(Protoss.CitadelOfAdun) && ! haveEver(Protoss.RoboticsFacility) && unitsEver(Protoss.Nexus) < 2)
  banPersonality(PersonalityCheese)
}
object PvPCoreExpand extends PvPStrategy {
  setMinimumGamesVsOpponent(3)
  addSelectionRequirement(() =>
    rushTilesMinimum >= 200
    || MapGroups.strongNatural.exists(_())
    ||   MacroFacts.enemyRecentStrategy(With.fingerprints.robo, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
    || ! MacroFacts.enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))
  addActivationRequirement(() => units(Protoss.Nexus) >= 2 && have(Protoss.CyberneticsCore) && unitsEver(Protoss.Gateway) < 2 && ! haveEver(Protoss.CitadelOfAdun, Protoss.RoboticsFacility))
  banPersonality(PersonalityCheese)
}
object PvP3GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  blacklistVs(With.fingerprints.dtRush)
  addSelectionRequirement(() => entranceInverted || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
  addActivationRequirement(() => units(Protoss.Gateway) >= 3 && unitsEver(Protoss.Nexus) < 2)
  banPersonality(PersonalityCheese)
}
object PvP4GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  blacklistVs(With.fingerprints.dtRush)
  addSelectionRequirement(() => entranceInverted || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
  addSelectionRequirement(() => entranceInverted || ! With.fingerprints.fourGateGoon.recently)
  addActivationRequirement(() => units(Protoss.Gateway) >= 4 && unitsEver(Protoss.Nexus) < 2)
}