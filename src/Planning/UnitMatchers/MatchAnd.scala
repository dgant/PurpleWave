package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchAnd(matchers: Matcher*) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    matchers.forall(_.apply(unit))
  }
  
}
