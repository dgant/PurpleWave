package Strategery.Strategies.Protoss

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Strategery.{Destination, MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvTStrategy extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
abstract class PvTBasicOpener extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseArbiter,
    PvT3BaseArbiter,
    PvT2BaseCarrier,
    PvT3BaseCarrier
  ))
}
object PvT13Nexus extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113)
}
object PvT21Nexus extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113,
    With.fingerprints.twoFac,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFac,
    With.fingerprints.threeFacVultures)
}
object PvT28Nexus extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs)
}
object PvT32Nexus extends PvTBasicOpener {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.bunkerRush,
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE
  )
}
object PvT2GateRangeExpand extends PvTBasicOpener {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoFac,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFac,
    With.fingerprints.threeFacVultures)
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE,
    With.fingerprints.fiveRax,
    With.fingerprints.bbs)
}
object PvT2GateRangeExpandCarrier extends PvTStrategy {
  override def allowedVsHuman: Boolean = false
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs)
}
object PvT25BaseCarrier extends PvTBasicOpener {
  override def allowedVsHuman: Boolean = false
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT3BaseCarrier
  ))
}
object PvT1015Expand extends PvTBasicOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination)
  override def entranceRamped: Boolean = false
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE
  )
}
object PvT1015DT extends PvTStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination)
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT3BaseArbiter,
      PvT3BaseCarrier))
  override def entranceRamped: Boolean = false
}
object PvT1GateRobo extends PvTBasicOpener
object PvTDTExpand extends PvTBasicOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT3BaseArbiter,
    PvT3BaseCarrier
  ))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures
  )
}
object PvT2BaseCarrier extends PvTStrategy {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.bio
  )
}
object PvT3BaseCarrier extends PvTStrategy
object PvT2BaseArbiter extends PvTStrategy
object PvT3BaseArbiter extends PvTStrategy

object PvTStove extends PvTStrategy {
  override def allowedVsHuman: Boolean = false
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT2BaseArbiter))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.bbs, With.fingerprints.twoRax1113)
}

object PvTProxy2Gate extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvtOpenersTransitioningFrom2Gate)
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}
