package Strategery.Strategies.Protoss

import Lifecycle.With
import Planning.Plans.Gameplans.Protoss.PvT.PvTCustom
import Planning.{MacroFacts, Plan}
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
object PvT13Nexus extends PvTStrategyNoZealotNoRange {
  setStartLocationsMin(4)
  setRushTilesMinimum(180)
}
object PvTRangeless     extends PvTStrategyNoZealotNoRange
object PvT28Nexus       extends PvTStrategyNoZealot
object PvTZealotExpand  extends PvTStrategy
object PvTZZCoreZ       extends PvTStrategy {
  addSelectionRequirement(() =>
    ! MacroFacts.enemyRecentStrategy(With.fingerprints.wallIn)
    || MacroFacts.enemyRecentStrategy(With.fingerprints.bbs))
}
object PvT910 extends PvTStrategy {
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
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures,
    With.fingerprints.bio,
    With.fingerprints.twoRaxAcad)
}
object PvTDT extends PvTStrategyNoZealot {
  blacklistVs(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures)
}
object PvT29Arbiter extends PvTStrategyNoZealot
object PvT4Gate extends PvTStrategyNoZealot {
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
object PvTProxy2Gate extends PvTStrategy {
  blacklistOn(MapGroups.badForProxying: _*)
  blacklistVs(With.fingerprints.wallIn)
}

object PvTCustom extends PvTStrategy {
  override def gameplan: Option[Plan] = Some(new PvTCustom)
}