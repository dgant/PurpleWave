package Strategery.Strategies.Protoss

import Lifecycle.With
import Macro.Facts.MacroFacts
import ProxyBwapi.Races.Protoss
import Strategery.MapGroups
import Strategery.Strategies.Personalities.{PersonalityBase, PersonalityCheese}
import Strategery.Strategies.Strategy
import Utilities.Time.Minutes
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Terran)
}

abstract class PvTStrategyNoZealot extends PvTStrategy {
  blacklistVs(
    With.fingerprints.workerRush,
    With.fingerprints.proxyRax,
    With.fingerprints.fiveRax,
    With.fingerprints.eightRax,
    With.fingerprints.bbs)
}
abstract class PvTStrategyNoZealotNoRange extends PvTStrategyNoZealot {
  blacklistVs(
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113)
}

//////////////
// Openings //
//////////////

object PvT13Nexus extends PvTStrategyNoZealotNoRange {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  setStartLocationsMin(4)
  setRushTilesMinimum(180)
}
object PvTRangeless     extends PvTStrategyNoZealotNoRange {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  banPersonality(PersonalityCheese)
}
object PvT28Nexus       extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  banPersonality(PersonalityCheese)
}
object PvTZealotExpand  extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  banPersonality(PersonalityCheese)
}
object PvTZZCoreZ       extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  addSelectionRequirement(() =>
    ! MacroFacts.enemyRecentStrategy(With.fingerprints.wallIn)
    || MacroFacts.enemyRecentStrategy(With.fingerprints.bbs))
  banPersonality(PersonalityCheese)
}
object PvT910 extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  setMinimumGamesVsOpponent(3)
  whitelistVs(
    With.fingerprints.workerRush,
    With.fingerprints.proxyRax,
    With.fingerprints.fiveRax,
    With.fingerprints.eightRax,
    With.fingerprints.bbs,
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
}
object PvT1015 extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  setRushTilesMaximum(200)
  setEntranceRamped(false)
  blacklistOn(MapGroups.badForMassGoon: _*)
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
  blacklistVs(
    With.fingerprints.wallIn)
}
object PvT1BaseReaver extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo)
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures,
    With.fingerprints.bio,
    With.fingerprints.twoRaxAcad)
  addActivationRequirement(() => have(Protoss.RoboticsFacility) && unitsEver(Protoss.Nexus) < 2)
}
object PvTDT extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTArbiter)
  blacklistVs(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures)
  addActivationRequirement(() => have(Protoss.CitadelOfAdun) && unitsEver(Protoss.Nexus) < 2)
}
object PvT29Arbiter extends PvTStrategyNoZealot {
  addChoice(PvTArbiter)
  addActivationRequirement(() => have(Protoss.CitadelOfAdun) && unitsEver(Protoss.Nexus) < 2)
  banPersonality(PersonalityBase)
}
object PvT4Gate extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTArbiter)
  setRushTilesMaximum(200)
  setEntranceRamped(false)
  blacklistOn(MapGroups.badForMassGoon: _*)
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoRaxAcad)
  blacklistVs(
    With.fingerprints.wallIn
  )
  addActivationRequirement(() => units(Protoss.Gateway) >= 4 && unitsEver(Protoss.Nexus) < 2)
}
object PvTGasSteal extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier, PvTArbiter)
  blacklistVs(
    With.fingerprints.workerRush,
    With.fingerprints.bbs)
  setStartLocationsMax(3)
  addActivationRequirement(() => have(u => Protoss.Assimilator(u) && u.base.exists(b => b.isEnemy && b.isMain) && u.frameDiscovered < Minutes(5)()))
}

//////////////////
// Compositions //
//////////////////

object PvTDoubleRobo  extends PvTStrategy
object PvTFastCarrier extends PvTStrategy
object PvTArbiter     extends PvTStrategy

//////////////
// Oddballs //
//////////////

object PvTSpeedlotDT extends PvTStrategy