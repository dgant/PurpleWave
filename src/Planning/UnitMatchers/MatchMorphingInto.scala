package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchMorphingInto(intoClass: UnitClass) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.friendly.exists(_.buildType == intoClass)
}
