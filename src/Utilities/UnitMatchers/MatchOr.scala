package Utilities.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchOr(matches: UnitMatcher*) extends UnitMatcher {
  @inline final override def apply(unit: UnitInfo): Boolean = matches.exists(_(unit))
}
