package Planning.Plans.Placement

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Planning.Plan
import Planning.Plans.Macro.Automatic.Pump
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass

class BuildZergStaticDefenseAtBases(towersRequired: Int, towerClass: UnitClass) extends Plan {
  
  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray
    
    if (zones.nonEmpty) {
      val creepColoniesRequired = zones.map(placeBuildingsInZone).sum
      new Pump(Zerg.CreepColony, maximumConcurrently = creepColoniesRequired)
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }
  
  private def placeBuildingsInZone(zone: Zone): Int = {
    lazy val towersInZone = With.units.countOursP(unit => Vector(Zerg.CreepColony, towerClass).contains(unit.unitClass) && unit.zone == zone)
    lazy val creepColoniesToAdd = towersRequired - towersInZone
    Math.max(0, creepColoniesToAdd)
  }
}
