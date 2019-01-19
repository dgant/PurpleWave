package Planning.UnitMatchers
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobile extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.canMove || unit.is(Terran.SiegeTankSieged) || (unit.unitClass.isFlyingBuilding && unit.flying)
}
