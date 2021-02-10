package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class Match(matches: (UnitInfo) => Boolean) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = matches(unit)
}
