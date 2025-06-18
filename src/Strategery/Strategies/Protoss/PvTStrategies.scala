package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvT.PvTSpeedlotDT
import Lifecycle.With
import Macro.Facts.MacroFacts
import Planning.Plans.Plan
import Strategery.MapGroups
import Strategery.Strategies.Strategy
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
  addChoice(PvTDoubleRobo, PvTFastCarrier)
  setStartLocationsMin(4)
  setRushTilesMinimum(180)
}
object PvTRangeless     extends PvTStrategyNoZealotNoRange {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
}
object PvT28Nexus       extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
}
object PvTZealotExpand  extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
}
object PvTZZCoreZ       extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
  addSelectionRequirement(() =>
    ! MacroFacts.enemyRecentStrategy(With.fingerprints.wallIn)
    || MacroFacts.enemyRecentStrategy(With.fingerprints.bbs))
}
object PvT910 extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
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
  addChoice(PvTDoubleRobo, PvTFastCarrier)
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
}
object PvTDT extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo)
  blacklistVs(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures)
}
object PvT29Arbiter extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
}
object PvT4Gate extends PvTStrategyNoZealot {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
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
}
object PvTGasSteal extends PvTStrategy {
  addChoice(PvTDoubleRobo, PvTFastCarrier)
  blacklistVs(
    With.fingerprints.workerRush,
    With.fingerprints.bbs)
  setStartLocationsMax(3)
}

//////////////////
// Compositions //
//////////////////

object PvTDoubleRobo extends PvTStrategy
object PvTFastCarrier extends PvTStrategy

//////////////
// Oddballs //
//////////////

object PvTProxy2Gate extends PvTStrategy {
  blacklistOn(MapGroups.badForProxying: _*)
  blacklistVs(With.fingerprints.wallIn)
}

object PvTCustom extends PvTStrategy {
  override def gameplan: Option[Plan] = Some(new PvTSpeedlotDT)
}