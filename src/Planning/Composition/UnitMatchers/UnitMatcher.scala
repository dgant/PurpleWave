package Planning.Composition.UnitMatchers

import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

trait UnitMatcher {
  def accept(unit:FriendlyUnitInfo): Boolean
}
