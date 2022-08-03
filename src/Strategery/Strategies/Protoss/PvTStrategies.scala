package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}

object PvTEndgameCarrier extends PvTStrategy
object PvTEndgameArbiter extends PvTStrategy
object PvTEndgameStorm extends PvTStrategy

object PvTMidgameCarrier extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTEndgameCarrier))
}
object PvTMidgameReaver extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm))
}
object PvTMidgameStorm extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm))
}
object PvTMidgameOpen extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTEndgameCarrier, PvTEndgameArbiter, PvTEndgameStorm))
}

abstract class PvTOpener extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTMidgameCarrier, PvTMidgameReaver, PvTMidgameStorm, PvTMidgameOpen))
}
object PvT13Nexus extends PvTOpener {
  override def startLocationsMin: Int = 4
  override def rushTilesMinimum: Int = 200
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113)
}
object PvTZealotExpand extends PvTOpener
object PvT28Nexus extends PvTOpener
object PvTZZCoreZ extends PvTOpener
object PvT1015 extends PvTOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForMassGoon
  override def rushTilesMaximum: Int = 200
  override def entranceRamped: Boolean = false
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE
  )
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}
object PvT1BaseReaver extends PvTOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTMidgameReaver))
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures,
    With.fingerprints.bio,
    With.fingerprints.twoRaxAcad)
}
object PvTDT extends PvTOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvTMidgameStorm, PvTMidgameOpen))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures
  )
}
object PvT4Gate extends PvTOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForMassGoon
  override def rushTilesMaximum: Int = 200
  override def entranceRamped: Boolean = false
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.twoRaxAcad
  )
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}
object PvTProxy2Gate extends PvTOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}
