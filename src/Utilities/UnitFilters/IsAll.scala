package Utilities.UnitFilters
import ProxyBwapi.UnitInfo.UnitInfo

case class IsAll(matchers: UnitFilter*) extends UnitFilter {
  
  override def apply(unit: UnitInfo): Boolean = {
    matchers.forall(_.apply(unit))
  }
  
}
