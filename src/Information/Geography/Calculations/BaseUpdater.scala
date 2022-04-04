package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.Time.Minutes

object BaseUpdater {
  
  def updateBase(base: Base) {
    if (base.townHallArea.tiles.exists(_.visibleUnchecked)) {
      base.lastScoutedFrame = With.frame
    }
    if (With.grids.enemyVision.inRange(base.townHallTile)) {
      base.lastScoutedByEnemyFrame = With.frame
    }
    base.units            = base.zone.units.filter(u => u.base.contains(base) && u.likelyStillThere).toVector
    base.townHall         = Maff.minBy(base.units.view.filter(u => u.unitClass.isTownHall && ! u.flying && u.tileTopLeft.tileDistanceFast(base.townHallTile) < 12))(_.tileTopLeft.tileDistanceManhattan(base.townHallTile))
    base.minerals         = base.units.filter(u => u.mineralsLeft > 0 && ! u.isBlocker)
    base.gas              = base.units.filter(_.unitClass.isGas)
    base.workerCount      = base.units.count(u => u.player == base.owner && u.is(IsWorker))
    base.mineralsLeft     = base.minerals.view.map(_.mineralsLeft).sum
    base.gasLeft          = base.gas.view.map(_.gasLeft).sum
    base.lastPlannedExpo  = if (base.plannedExpo()) With.frame else base.lastPlannedExpo
    base.enemyCombatValue = base.units.view.filter(_.isEnemy).filter(IsWarrior).map(_.subjectiveValue).sum

    updateOwner(base)
  }
  
  private def updateOwner(base: Base) {

    val originalOwner = base.owner
    
    // Derive the owner from the current town hall
    // If we have previously inferred the base's owner, maintain the inference
    if (base.townHall.exists(_.player != base.owner)) {
      val hall = base.townHall.get
      With.logger.debug(f"Detecting ownership of base from visible town hall: $hall")
      base.owner = hall.player
    } else {

      val scoutingNow = base.lastScoutedFrame == With.frame
      val framesSinceScouting = With.framesSince(base.lastScoutedFrame)
      val hiddenNaturalDelay = Minutes(3)()

      if (scoutingNow) {
        if (base.townHall.isEmpty && ! base.owner.isNeutral) {
          base.owner = With.neutral
          With.logger.debug(f"Detecting absent base: $base")
        }
      } else if (base.owner.isNeutral) {

        if (With.scouting.enemyMain.contains(base)) {
          base.owner = With.enemy
          With.logger.debug(f"Assuming ${base.owner} owns $base as implicit starting location")
        }

        if (framesSinceScouting > hiddenNaturalDelay) {
          val building = base.zone.units.find(u => u.isEnemy && ! u.flying && u.unitClass.isBuilding)
          if (building.isDefined && With.frame > base.lastScoutedFrame + hiddenNaturalDelay) {
            base.owner = building.get.player
            With.logger.debug(f"Assuming ${base.owner} owns $base last scouted at ${base.lastScoutedFrame} due to presence of $building")
          }
        }

        if ( ! With.self.isZerg
          && With.scouting.weExpandedFirst
          && base.isNaturalOf.exists(With.scouting.enemyMain.contains)
          && With.frame > Math.max(With.scouting.firstExpansionFrameUs, base.lastScoutedFrame) + hiddenNaturalDelay) {
          base.owner = base.isNaturalOf.get.owner
          With.logger.debug(f"Assuming ${base.owner} has taken $base last scouted at ${base.lastScoutedFrame} after we expanded on ${With.scouting.firstExpansionFrameUs}")
        }

        if ( ! base.scouted && base.owner.bases.forall(base.natural.contains)) {
          base.natural.filter(_.owner.isEnemy).foreach(natural => {
            base.owner = natural.owner
            With.logger.debug(f"Assuming ${base.owner} owns unscouted main $base due to possession of its natural {$base.natural}")
          })
        }
      }
    }

    if (originalOwner != base.owner) {
      base.lastOwnerChangeFrame = With.frame
    }
  }
}
