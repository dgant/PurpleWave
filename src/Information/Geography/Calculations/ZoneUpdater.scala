package Information.Geography.Calculations

import Information.Geography.Types.{Base, Zone}
import Startup.With
import Utilities.EnrichPosition._

object ZoneUpdater {
  
  def update() = {
    With.geography.zones.foreach(updateZone)
  }
  
  private def updateZone(zone:Zone) {
    zone.bases.foreach(updateBase)
  }
  
  private def updateBase(base:Base) {
    updateTownHall(base)
    updateOwner(base)
    updateResources(base)
  }
  
  private def updateTownHall(base: Base) {
    base.townHall = With.units.buildings
      .filter(_.unitClass.isTownHall)
      .filter(townHall => base.zone.contains(townHall.pixelCenter))
      .toList
      .sortBy(_.tileDistance(base.townHallRectangle.midpoint))
      .headOption
  }
  
  private def updateOwner(base: Base) {
    base.zone.owner = base.townHall.map(_.player).getOrElse(With.game.neutral)
  }
  
  private def updateResources(base: Base) {
    base.minerals = With.units.neutral
      .filter(unit => unit.unitClass.isMinerals && base.zone.contains(unit.pixelCenter))
      .toSet
    base.gas = With.units.all
      .filter(unit => unit.unitClass.isGas && base.zone.contains(unit.pixelCenter))
  
    base.mineralsLeft   = base.minerals.filter(_.alive).toList.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.filter(_.alive).toList.map(_.gasLeft).sum
    base.harvestingArea = (List(base.townHallRectangle) ++ (base.minerals ++ base.gas).map(_.tileArea)).boundary
  }
}
