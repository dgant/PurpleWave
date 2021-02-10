package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchNot(matcher: Matcher) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = ! matcher.apply(unit)
  
}
