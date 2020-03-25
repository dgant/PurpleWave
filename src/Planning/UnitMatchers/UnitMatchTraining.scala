package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchTraining(unitClass: UnitClass) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.friendly.exists(_.trainee.exists(_.is(unitClass)))
}
