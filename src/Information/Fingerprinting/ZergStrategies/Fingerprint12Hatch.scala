package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwelveHatch_HatchCompleteBy
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Seconds
import Utilities.UnitFilters.UnitFilter

class Fingerprint12Hatch extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourPool),
  new FingerprintNot(With.fingerprints.ninePool),
  new FingerprintNot(With.fingerprints.overpool),
  new FingerprintNot(With.fingerprints.twelvePool),
  new FingerprintNot(With.fingerprints.tenHatch),
  new FingerprintOr(
    new FingerprintCompleteBy(HatchCriterion, TwelveHatch_HatchCompleteBy + Seconds(10)),
    With.fingerprints.hatchFirst))

private object HatchCriterion extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft)
}