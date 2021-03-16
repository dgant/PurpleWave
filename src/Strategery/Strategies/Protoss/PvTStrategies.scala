package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
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
    PvT2BaseReaver,
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT2BaseGateway,
    PvT3rdFast,
    PvT3rdObs,
  ))
}

abstract class PvTEndgame extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(PvEStormOptions())
}

object PvT13Nexus extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.bunkerRush,
    With.fingerprints.twoRax1113)
}
object PvT24Nexus extends PvTBasicOpener
object PvT32Nexus extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}
object PvT4Gate extends PvTBasicOpener
object PvT2GateRangeExpand extends PvTBasicOpener {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoRax1113,
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFac,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFac,
    With.fingerprints.threeFacVultures)
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fiveRax,
    With.fingerprints.bbs,
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
}
object PvT1015Expand extends PvTBasicOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination)
  override def entranceRamped: Boolean = false
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE
  )
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}
object PvT1015DT extends PvTStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = Seq(Destination)
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT2BaseGateway,
      PvT3rdFast))
}
object PvT1GateReaver extends PvTBasicOpener {
  override def responsesWhitelisted: Iterable[Fingerprint] = {
    // TODO: Add FD once we have a fingerprint
    Seq(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE, With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures, With.fingerprints.bio, With.fingerprints.twoRaxAcad)
  }
  override def responsesBlacklisted: Iterable[Fingerprint] = {
    // TODO: Add 1 Fac Port
    // TODO: Add 2 Fac tank?
    Seq(With.fingerprints.siegeExpand)
  }
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT2BaseReaver, PvT3rdObs))
}
object PvTDTExpand extends PvTBasicOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT2BaseGateway,
    PvT3rdFast
  ))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoRaxAcad,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFacVultures
  )
}
object PvTStove extends PvTStrategy {
  override def allowedVsHuman: Boolean = false
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT2BaseArbiter))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.bbs, With.fingerprints.twoRax1113)
}
object PvTProxy2Gate extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT2BaseReaver,
    PvT3rdFast,
    PvT3rdObs
  ))
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.wallIn
  )
}

object PvT2BaseReaver extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT2BaseCarrier, PvT3BaseArbiter, PvT3BaseCarrier, PvT3BaseGateway))
}
object PvT3rdFast extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT3BaseArbiter, PvT3BaseCarrier, PvT3BaseGateway))
}
object PvT3rdObs extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT3BaseArbiter, PvT3BaseCarrier, PvT3BaseGateway))
}
object PvT2BaseCarrier extends PvTEndgame
object PvT2BaseArbiter extends PvTEndgame
object PvT2BaseGateway extends PvTEndgame { override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.oneArmoryUpgrades, With.fingerprints.twoArmoryUpgrades) }
object PvT3BaseCarrier extends PvTEndgame
object PvT3BaseArbiter extends PvTEndgame
object PvT3BaseGateway extends PvTEndgame { override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.oneArmoryUpgrades, With.fingerprints.twoArmoryUpgrades) }

