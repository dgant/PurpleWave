package Utilities.UnitFilters
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object IsCombatSpellcaster extends UnitFilter {
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
