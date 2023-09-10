package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwelveHatch_HatchCompleteBy
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Seconds
import Utilities.UnitFilters.UnitFilter

class Fingerprint12Hatch extends FingerprintAnd(

  new FingerprintNot(
    With.fingerprints.fourPool,
    With.fingerprints.ninePool,
    With.fingerprints.overpool,
    With.fingerprints.twelvePool,
    With.fingerprints.tenHatch),

  new FingerprintOr(
    new FingerprintCompleteBy(IsNonStartingHatch, TwelveHatch_HatchCompleteBy + Seconds(10)),
    With.fingerprints.hatchFirst))

private object IsNonStartingHatch extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft)
}