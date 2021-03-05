package Planning.UnitMatchers
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobile extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = (
    (unit.canMove || unit.is(Terran.SiegeTankSieged) || unit.burrowed)
    && ! unit.isAny(Protoss.Interceptor, Protoss.Scarab, Zerg.Larva))
}
