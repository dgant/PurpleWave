package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo

trait UnitMatcher {
  def accept(unit:FriendlyUnitInfo): Boolean
}
