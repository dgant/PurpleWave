package Strategery.Strategies.Protoss

import Lifecycle.With
import Strategery.MapGroups
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Terran)
}

object PvTEndgameCarrier  extends PvTStrategy
object PvTEndgameArbiter  extends PvTStrategy
object PvTEndgameStorm    extends PvTStrategy

object PvTMidgameCarrier  extends PvTStrategy { setChoice(PvTEndgameCarrier); blacklistVs(With.fingerprints.bio) }
object PvTMidgameReaver   extends PvTStrategy { setChoice(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm) }
object PvTMidgameStorm    extends PvTStrategy { setChoice(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm); whitelistVs(With.fingerprints.bio) }
object PvTMidgameOpen     extends PvTStrategy { setChoice(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm) }

abstract class PvTOpener extends PvTStrategy {
  setChoice(PvTMidgameCarrier, PvTMidgameReaver, PvTMidgameStorm, PvTMidgameOpen)
}
abstract class PvTOpenerNoZealot extends PvTOpener {
  setMinimumGamesVsOpponent(1)
  blacklistVs(
    With.fingerprints.workerRush,
    With.fingerprints.proxyRax,
    With.fingerprints.fiveRax,
    With.fingerprints.eightRax,
    With.fingerprints.bbs)
}
abstract class PvTOpenerNoZealotNoRange extends PvTOpenerNoZealot {
  blacklistVs(
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113)
}
object PvT13Nexus extends PvTOpenerNoZealotNoRange {
  setStartLocationsMin(4)
  setRushTilesMinimum(180)
}
object PvTRangeless     extends PvTOpenerNoZealotNoRange
object PvT28Nexus       extends PvTOpenerNoZealot
object PvTZealotExpand  extends PvTOpener
object PvTZZCoreZ       extends PvTOpener
object PVT910           extends PvTOpener {
  /*
  whitelistVs(
    With.fingerprints.workerRush,
    With.fingerprints.proxyRax,
    With.fingerprints.fiveRax,
    With.fingerprints.eightRax,
    With.fingerprints.bbs,
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
    */
}
object PvT1015 extends PvTOpenerNoZealot {
  setRushTilesMaximum(200)
  setEntranceRamped(false)
  blacklistOn(MapGroups.badForMassGoon: _*)
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
  blacklistVs(
    With.fingerprints.wallIn)
}
object PvT1BaseReaver extends PvTOpenerNoZealot {
  setChoice(PvTMidgameReaver)
  whitelistVs(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures,
    With.fingerprints.bio,
    With.fingerprints.twoRaxAcad)
}
object PvTDT extends PvTOpenerNoZealot {
  setChoice(PvTMidgameStorm, PvTMidgameOpen)
  blacklistVs(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures)
}
object PvT4Gate extends PvTOpenerNoZealot {
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
object PvTProxy2Gate extends PvTOpener {
  blacklistOn(MapGroups.badForProxying: _*)
  blacklistVs(With.fingerprints.wallIn)
}
