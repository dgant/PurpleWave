package Processes

import Startup.With
import bwapi.UnitType

class Map {
  
  val _townHallTypes = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  )
  
  def isTownHall(unitType:UnitType):Boolean = _townHallTypes.contains(unitType)
  
  def ourBaseHalls:Iterable[bwapi.Unit] = {
    With.ourUnits.filter(unit => isTownHall(unit.getType) && ! unit.isFlying)
  }
}
