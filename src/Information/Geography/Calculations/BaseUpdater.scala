package Information.Geography.Calculations

import Information.Geography.Types.Base
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.Protoss
import Utilities.ByOption
import Utilities.EnrichPixel._

object BaseUpdater {
  
  def updateBase(base: Base) {
    updateAssets(base)
    updateOwner(base)
  }
  
  private def updateOwner(base: Base) {
    
    // Derive the owner from the current town hall
    // If we have previously inferred the base's owner, maintain the inference
    base.owner = base.townHall.map(_.player).getOrElse(if (base.scouted) With.neutral else base.owner)
    
    // Assume ownership of implicit starting location
    if (base.owner.isNeutral && base.lastScoutedFrame <= 0 && With.intelligence.firstEnemyMain.contains(base)) {
      base.owner = With.enemy
    }
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && With.framesSince(base.lastScoutedFrame) > Protoss.Nexus.buildFrames) {
      val building = base.zone.units.find(unit => unit.isEnemy && ! unit.flying && unit.unitClass.isBuilding)
      if (building.exists(_.lastSeen > base.lastScoutedFrame + GameTime(3, 0)())) {
        base.owner = building.get.player
      }
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
    base.workers        = base.units.filter(u => u.player == base.owner && u.is(UnitMatchWorkers))
    base.defenders      = base.units.filter(u => u.player == base.owner && u.unitClass.rawCanAttack)
    base.mineralsLeft   = base.minerals.iterator.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.iterator.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
  }
}
