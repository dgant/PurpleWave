package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchMorphingInto(intoClass: UnitClass) extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = unit.friendly.exists(_.buildType == intoClass)
}
