package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

object BaseUpdater {
  
  def updateBase(base: Base) {
    updateTownHall(base)
    updateOwner(base)
    updateAssets(base)
  }
  
  private def updateTownHall(base: Base) {
    base.townHall = None
    val townHalls = With.units.buildings
      .filter(unit =>
        unit.unitClass.isTownHall
        && unit.tileIncludingCenter.zone == base.zone
        && base.zone.bases.minBy(_.heart.tileDistanceFast(unit.tileIncludingCenter)) == base)
    
    if (townHalls.nonEmpty) {
      base.townHall = Some(townHalls.minBy(_.pixelDistanceSquared(base.townHallArea.midPixel)))
    }
  }
  
  private def updateOwner(base: Base) {
    
    base.owner = base.townHall.map(_.player).getOrElse(With.neutral)
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && base.lastScoutedFrame < With.framesSince(Protoss.Nexus.buildFrames)) {
      With.units.enemy
        .find(unit => ! unit.flying && unit.unitClass.isBuilding && unit.pixelCenter.zone == base.zone)
        .foreach(enemyBuilding => base.owner = enemyBuilding.player)
    }
    
    // Assume ownership of unscouted main from natural
    if (base.owner.isNeutral && ! base.scouted) {
      val natural = With.geography.bases.find(_.isNaturalOf.contains(base))
      natural.foreach(someNatural => base.owner = someNatural.owner)
    }
  }
  
  private def updateAssets(base: Base) {
    base.minerals       = With.units.neutral.filter(u => u.mineralsLeft > 0 && ! u.isMineralBlocker && resourceIsInBase(u, base)).toSet
    base.gas            = With.units.all.filter(_.unitClass.isGas).filter(resourceIsInBase(_, base))
    base.workers        = With.units.all.filter(unit => unit.unitClass.isWorker && base.zone.contains(unit.pixelCenter))
    base.mineralsLeft   = base.minerals.filter(_.alive).toVector.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.filter(_.alive).toVector.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
  }
  
  private def resourceIsInBase(resource: UnitInfo, base: Base): Boolean = {
    resource.pixelCenter.zone == base.townHallTile.zone &&
    resource.pixelDistanceFast(base.townHallArea.midPixel) < With.configuration.baseRadiusPixels
  }
  
}
