package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchCustom(matches: (UnitInfo) => Boolean) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = matches(unit)
}
