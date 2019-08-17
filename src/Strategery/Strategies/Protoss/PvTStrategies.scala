package Strategery.Strategies.Protoss

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTReaverCarrierCheese
import Strategery.Strategies.Strategy
import Strategery.{BlueStorm, Destination, MapGroups, StarCraftMap}
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
    With.fingerprints.bbs)
  override def startLocationsMin: Int = 4
}
object PvT21Nexus extends PvTBasicOpener
object PvT23Nexus extends PvTBasicOpener
object PvT28Nexus extends PvTBasicOpener
object PvT2GateRangeExpand extends PvTBasicOpener {
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE)
}
object PvT2GateRangeExpandCarrier extends PvTStrategy
object PvT25BaseCarrier extends PvTBasicOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT3BaseCarrier
  ))
}
object PvT1015Expand extends PvTBasicOpener {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits :+ Destination
  override def entranceRamped: Boolean = false

  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.fourteenCC,
    With.fingerprints.oneRaxFE
  )
}
object PvT1015DT extends PvTStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForBigUnits :+ Destination
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT3BaseArbiter,
      PvT3BaseCarrier))
  override def entranceRamped: Boolean = false
}
object PvT1GateRobo extends PvTBasicOpener
object PvT2GateObserver extends PvTBasicOpener {
  override def responsesWhitelisted: Iterable[Fingerprint] = Seq(
    With.fingerprints.twoFac,
    With.fingerprints.twoFacVultures,
    With.fingerprints.threeFac,
    With.fingerprints.threeFacVultures,
  )
}
object PvTDTExpand extends PvTBasicOpener {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    PvT2BaseArbiter,
    PvT3BaseArbiter,
    PvT3BaseCarrier
  ))
}

object PvT2BaseCarrier extends PvTStrategy
object PvT3BaseCarrier extends PvTStrategy { override val mapsBlacklisted = MapGroups.badForFastThirdBases }
object PvT2BaseArbiter extends PvTStrategy { override val mapsBlacklisted = Iterable(BlueStorm) }
object PvT3BaseArbiter extends PvTStrategy { override val mapsBlacklisted = MapGroups.badForFastThirdBases }

object PvTStove extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(PvT2BaseArbiter))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.bbs, With.fingerprints.twoRax1113)
}

object PvTProxy2Gate extends PvTStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvtOpenersTransitioningFrom2Gate)
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}

object PvTReaverCarrierCheese extends PvTStrategy {
  override def gameplan: Option[Plan] = Some(new PvTReaverCarrierCheese)
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Vector("Rooijackers", "Leta"))
}
