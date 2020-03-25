package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchAnd(matchers: UnitMatcher*) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    matchers.forall(_.apply(unit))
  }
  
}
