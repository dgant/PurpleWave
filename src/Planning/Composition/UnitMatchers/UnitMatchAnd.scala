package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitMatchAnd(matches: Iterable[UnitMatcher]) extends UnitMatcher {
  
  override def accept(unit: FriendlyUnitInfo): Boolean =
    matches.forall(_.accept(unit))
  
}
