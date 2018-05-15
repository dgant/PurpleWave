package Planning.Composition.UnitMatchers
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobile extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.canMove || unit.is(Terran.SiegeTankSieged)
}
