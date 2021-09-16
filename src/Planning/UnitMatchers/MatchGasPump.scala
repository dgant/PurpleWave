package Planning.UnitMatchers
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object MatchGasPump extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.isAny(Terran.Refinery, Protoss.Assimilator, Zerg.Extractor)
}
