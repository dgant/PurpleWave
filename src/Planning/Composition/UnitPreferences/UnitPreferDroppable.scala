package Planning.Composition.UnitPreferences
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferDroppable extends UnitPreference {
  
  def preferenceOrder: Array[UnitClass] = Array(
    Zerg.InfestedTerran,
    Protoss.Reaver,
    Protoss.Zealot,
    Protoss.HighTemplar,
    Zerg.Lurker,
    Protoss.DarkTemplar,
    Terran.Vulture,
    Terran.Firebat,
    Terran.Marine,
    Terran.Goliath,
    Terran.Medic,
    Protoss.Archon,
    Zerg.Zergling,
    Zerg.Hydralisk,
    Zerg.Ultralisk,
    Terran.SiegeTankUnsieged,
    Terran.SiegeTankSieged
  )
  
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    val index = preferenceOrder.indexOf(unit.unitClass)
    if (index < 0)
      Double.MaxValue
    else
      index
  }
}
