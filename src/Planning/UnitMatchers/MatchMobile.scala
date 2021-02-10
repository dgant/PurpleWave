package Planning.UnitMatchers
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobile extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = (
    unit.unitClass.canMove
    || unit.is(Terran.SiegeTankSieged)
    || (unit.unitClass.isFlyingBuilding && unit.flying))
}
