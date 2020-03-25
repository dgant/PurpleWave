package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobile extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.canMove || unit.isSiegeTankSieged() || (unit.unitClass.isFlyingBuilding && unit.flying)
}
