package Strategies.UnitMatchers
import Types.UnitInfo.FriendlyUnitInfo

object UnitMatchAnything extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = true
}
