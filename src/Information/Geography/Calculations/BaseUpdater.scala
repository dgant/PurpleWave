package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Protoss
import Utilities.Minutes

object BaseUpdater {
  
  def updateBase(base: Base) {
    if (base.townHallTile.visibleUnchecked) {
      base.lastScoutedFrame = With.frame
    }

    if (With.grids.enemyVision.inRange(base.townHallTile)) {
      base.lastScoutedByEnemyFrame = With.frame
    }

    base.units            = base.zone.units.filter(u => u.base.contains(base) && u.likelyStillThere).toVector
    base.townHall         = Maff.minBy(base.units.view.filter(u => u.unitClass.isTownHall && ! u.flying))(_.tileTopLeft.tileDistanceManhattan(base.townHallTile))
    base.minerals         = base.units.filter(u => u.mineralsLeft > 0 && ! u.isBlocker)
    base.gas              = base.units.filter(_.unitClass.isGas)
    base.workerCount      = base.units.count(u => u.player == base.owner && u.is(MatchWorker))
    base.defenseValue     = base.units.view.filter(_.player == base.owner).filterNot(MatchWorker).filter(_.unitClass.attacksOrCastsOrDetectsOrTransports).map(_.subjectiveValue).sum
    base.mineralsLeft     = base.minerals.view.map(_.mineralsLeft).sum
    base.gasLeft          = base.gas.view.map(_.gasLeft).sum
    base.lastPlannedExpo  = if (base.plannedExpo()) With.frame else base.lastPlannedExpo

    updateOwner(base)
  }
  
  private def updateOwner(base: Base) {

    val originalOwner = base.owner
    
    // Derive the owner from the current town hall
    // If we have previously inferred the base's owner, maintain the inference
    base.owner = base.townHall.map(_.player).getOrElse(if (base.scouted) With.neutral else base.owner)
    
    // Assume ownership of implicit starting location
    if (base.owner.isNeutral && With.scouting.enemyMain.contains(base)) {
      base.owner = With.enemy
    }
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && With.framesSince(base.lastScoutedFrame) > Protoss.Nexus.buildFrames) {
      val building = base.zone.units.find(unit => unit.isEnemy && ! unit.flying && unit.unitClass.isBuilding)
      if (building.exists(_.lastSeen > base.lastScoutedFrame + Minutes(3)())) {
        base.owner = building.get.player
      }
    }
    
    // Assume ownership of unscouted main from natural if we haven't found any main yet
    if (base.owner.isNeutral && ! base.scouted && base.owner.bases.forall(base.natural.contains)) {
      base.natural.filter(_.owner.isEnemy).foreach(natural => base.owner = natural.owner)
    }

    if (originalOwner != base.owner) {
      base.lastOwnerChangeFrame = With.frame
    }
  }
}
