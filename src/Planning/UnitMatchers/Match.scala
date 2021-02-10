package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class Match(matches: (UnitInfo) => Boolean) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = matches(unit)
}
