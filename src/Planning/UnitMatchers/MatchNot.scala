package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchNot(matchers: UnitMatcher*) extends UnitMatcher {
  @inline final override def apply(unit: UnitInfo): Boolean = ! unit.isAny(matchers: _*)
  
}
