package Planning.UnitMatchers
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchCombatSpellcaster extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.isAny(
    Terran.ScienceVessel,
    Terran.Medic,
    Protoss.HighTemplar,
    Protoss.Arbiter,
    Protoss.DarkArchon,
    Zerg.Queen,
    Zerg.Defiler
  )
}
