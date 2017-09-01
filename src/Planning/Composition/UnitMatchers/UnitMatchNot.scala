package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchNot(matcher: UnitMatcher) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = ! matcher.accept(unit)
  
}
