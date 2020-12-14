package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchOr(matches: UnitMatcher*) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = matches.exists(_(unit))
}
