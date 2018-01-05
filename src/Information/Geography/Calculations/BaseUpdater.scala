package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.ByOption
import Utilities.EnrichPixel._

object BaseUpdater {
  
  def updateBase(base: Base) {
    updateAssets(base)
    updateOwner(base)
  }
  
  private def updateOwner(base: Base) {
    
    base.owner = base.townHall.map(_.player).getOrElse(With.neutral)
    
    // Assume ownership of implicit starting location
    if (base.owner.isNeutral && base.lastScoutedFrame <= 0 && With.intelligence.firstEnemyMain.contains(base)) {
      base.owner = With.enemy
    }
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && With.framesSince(base.lastScoutedFrame) > Protoss.Nexus.buildFrames) {
      base.zone.units
        .find(unit => unit.isEnemy && ! unit.flying && unit.unitClass.isBuilding)
        .foreach(enemyBuilding => base.owner = enemyBuilding.player)
    }
    
    // Assume ownership of unscouted main from natural
    if (base.owner.isNeutral && ! base.scouted) {
      base.natural.filter(_.owner.isEnemy).foreach(natural => base.owner = natural.owner)
    }
  }
  
  private def updateAssets(base: Base) {
    base.units          = base.zone.units.filter(_.base.contains(base))
    base.townHall       = ByOption.minBy(base.units.filter(u => u.unitClass.isTownHall && ! u.flying))(_.tileTopLeft.tileDistanceManhattan(base.townHallTile))
    base.minerals       = base.units.filter(u => u.mineralsLeft > 0 && ! u.isMineralBlocker)
    base.gas            = base.units.filter(_.unitClass.isGas)
    base.workers        = base.units.filter(u => u.player == base.owner && u.unitClass.isWorker)
    base.defenders      = base.units.filter(u => u.player == base.owner && u.unitClass.helpsInCombat)
    base.mineralsLeft   = base.minerals.toSeq.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.toSeq.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
  }
}
