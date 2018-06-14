package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchAnd(matchers: UnitMatcher*) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = matchers.forall(_.accept(unit))
  
}
