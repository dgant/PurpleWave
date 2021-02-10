package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchAnd(matchers: UnitMatcher*) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    matchers.forall(_.apply(unit))
  }
  
}
