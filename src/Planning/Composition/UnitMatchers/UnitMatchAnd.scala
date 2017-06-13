package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchAnd(matches: Iterable[UnitMatcher]) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    matches.forall(_.accept(unit))
  
}
