package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchOr(matches: Matcher*) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = matches.exists(_(unit))
}
