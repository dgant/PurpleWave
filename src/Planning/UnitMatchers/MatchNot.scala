package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchNot(matcher: UnitMatcher) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = ! matcher.apply(unit)
  
}
