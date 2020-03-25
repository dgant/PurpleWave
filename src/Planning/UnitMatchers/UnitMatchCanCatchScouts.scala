package Planning.UnitMatchers

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchCanCatchScouts extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.attacksGround
    && (
      unit.topSpeed > Terran.SCV.topSpeed || unit.pixelRangeGround >= 32.0 * 4.0)
    )
}