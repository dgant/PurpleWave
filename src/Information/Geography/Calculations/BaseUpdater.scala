package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.ByOption
import Utilities.EnrichPixel._

object BaseUpdater {
  
  def updateBase(base: Base) {
    base.townHall = ByOption.minBy(base.units.filter(_.unitClass.isTownHall))(_.tileTopLeft.tileDistanceManhattan(base.townHallTile))
    updateOwner(base)
    updateAssets(base)
  }
  
  private def updateOwner(base: Base) {
    
    base.owner = base.townHall.map(_.player).getOrElse(With.neutral)
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && base.lastScoutedFrame < With.framesSince(Protoss.Nexus.buildFrames)) {
      base.zone.units
        .find(unit => unit.isEnemy && ! unit.flying && unit.unitClass.isBuilding && unit.zone == base.zone)
        .foreach(enemyBuilding => base.owner = enemyBuilding.player)
    }
    
    // Assume ownership of unscouted main from natural
    if (base.owner.isNeutral && ! base.scouted) {
      val natural = With.geography.bases.find(_.isNaturalOf.contains(base))
      natural.filterNot(_.owner.isFriendly).foreach(someNatural => base.owner = someNatural.owner)
    }
  }
  
  private def updateAssets(base: Base) {
    base.units          = base.zone.units.filter(_.base.contains(base))
    base.minerals       = base.units.filter(u => u.mineralsLeft > 0 && ! u.isMineralBlocker)
    base.gas            = base.units.filter(u => u.unitClass.isGas)
    base.workers        = base.units.filter(u => u.unitClass.isWorker)
    base.mineralsLeft   = base.minerals.toSeq.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.toSeq.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
  }
}
