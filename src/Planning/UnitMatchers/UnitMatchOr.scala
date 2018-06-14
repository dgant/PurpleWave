package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchOr(matches: UnitMatcher*) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    matches.exists(_.accept(unit))
  
}
