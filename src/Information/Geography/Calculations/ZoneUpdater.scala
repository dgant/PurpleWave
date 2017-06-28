package Information.Geography.Calculations

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.EnrichPixel._

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
    updateAssets(base)
  }
  
  private def updateTownHall(base: Base) {
    base.townHall = None
    val townHalls = With.units.buildings
      .filter(unit =>
        unit.unitClass.isTownHall &&
        unit.tileIncludingCenter.zone == base.zone &&
        base.zone.contains(unit.pixelCenter))
    
    if (townHalls.nonEmpty) {
      base.townHall = Some(townHalls.minBy(_.pixelDistanceSquared(base.townHallArea.midPixel)))
    }
  }
  
  private def updateOwner(base: Base) {
    
    base.zone.owner = base.townHall.map(_.player).getOrElse(With.neutral)
    
    if (base.zone.owner.isNeutral && base.lastScoutedFrame < With.frame - Protoss.Nexus.buildFrames) {
      With.units.enemy
        .find(unit => ! unit.flying && unit.unitClass.isBuilding && unit.pixelCenter.zone == base.zone)
        .foreach(enemyUnit => base.zone.owner = enemyUnit.player)
    }
  }
  
  private def updateAssets(base: Base) {
    
    base.minerals       = With.units.neutral.filter(unit => unit.unitClass.isMinerals && base.zone.contains(unit.pixelCenter) && unit.mineralsLeft > 0).toSet
    base.gas            = With.units.all.filter(unit => unit.unitClass.isGas && base.zone.contains(unit.pixelCenter))
    base.workers        = With.units.all.filter(unit => unit.unitClass.isWorker && base.zone.contains(unit.pixelCenter))
    base.mineralsLeft   = base.minerals.filter(_.alive).toVector.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.filter(_.alive).toVector.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
    
    val exitBuildings = base.zone.exit.map(exit =>
      With.units
        .inTileRadius(exit.centerPixel.tileIncluding, 6)
        .filter(u => u.unitClass.isBuilding && ! u.flying))
      .getOrElse(List.empty)
    
    base.walledIn = exitBuildings.count(_.is(Terran.SupplyDepot)) >= 2 && exitBuildings.count(_.is(Terran.Barracks)) >= 1
    base.planningToTake = With.units.ours.exists(unit =>
      unit.actionState.toBuildTile.exists(_.zone == base.zone) &&
      unit.actionState.toBuild.exists(_.isTownHall))
  }
}
