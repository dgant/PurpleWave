package Strategery.Strategies.Protoss

import Lifecycle.With
import Planning.MacroFacts
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
  addRequirement(() => CanGateCoreOnThisMap())
}

object PvP1012 extends PvPStrategy {
  addChoice(PvP3Zealot, PvP5Zealot)
  addRequirement(() =>
    ! CanGateCoreOnThisMap()
    || With.fingerprints.nexusFirst.recently
    || With.fingerprints.proxyGateway.recently
    || With.fingerprints.twoGate99.recently)
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
  setMinimumGamesVsOpponent(3)
  addChoice(PvPGateCore)
  addRequirement(() =>
    rushTilesMinimum >= 200
    || MapGroups.strongNatural.exists(_())
    ||   MacroFacts.enemyRecentStrategy(With.fingerprints.robo, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
    || ! MacroFacts.enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))
}
object PvP3GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.dtRush)
  addRequirement(() => entranceInverted
    || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
}
object PvP4GateGoon extends PvPStrategy {
  setMinimumGamesVsOpponent(1)
  addChoice(PvP1012, PvPGateCore)
  blacklistVs(With.fingerprints.dtRush)
  addRequirement(() => entranceInverted || ! (MapGroups.badForMassGoon ++ MapGroups.strongNatural).exists(_()))
  addRequirement(() => entranceInverted || ! With.fingerprints.fourGateGoon.recently)
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

