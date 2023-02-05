package Information.Geography.Calculations

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Zerg
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsBuilding, IsWarrior, IsWorker}

object UpdateBase {
  
  def apply(base: Base): Unit = {
    base.units            = base.zone.units.filter(u => u.base.contains(base) && u.likelyStillThere).toVector
    base.townHall         = Maff.minBy(base.units.view.filter(u => u.unitClass.isTownHall && ! u.flying && u.tileTopLeft.tileDistanceFast(base.townHallTile) < 12))(_.tileTopLeft.tileDistanceManhattan(base.townHallTile))
    base.minerals         = base.units.filter(u => u.mineralsLeft > 0 && ! u.isBlocker)
    base.gas              = base.units.filter(_.unitClass.isGas)
    base.workerCount      = base.units.count(u => u.player == base.owner && u.is(IsWorker))
    base.mineralsLeft     = base.minerals.view.map(_.mineralsLeft).sum
    base.gasLeft          = base.gas.view.map(_.gasLeft).sum
    base.startingMinerals = Math.max(base.startingMinerals, base.mineralsLeft)
    base.startingGas      = Math.max(base.startingGas,      base.gasLeft)
    base.enemyCombatValue = base.units.view.filter(_.isEnemy).filter(IsWarrior).map(_.subjectiveValue).sum

    // Check for planned expansions
    // Most of this logic is ensuring we don't count buildings placed just to *block* expansions, eg. Pylons
    if (base.owner.isNeutral) {
      var planningExpo = false
      planningExpo ||=
        With.units.ours.map(_.intent).exists(intent =>
          intent.toBuildTile.filter(_.base.contains(base)).exists(tile =>
            intent.toBuild.exists(_.isTownHall) || ! base.townHallArea.contains(tile)))
      planningExpo ||= base.ourUnits.filter(IsBuilding).exists( ! _.tileArea.intersects(base.townHallArea))
      if (planningExpo) {
        base.lastPlannedExpo = With.frame
      }
    }

    if (base.townHallArea.tiles.exists(_.visibleUnchecked)) {
      base.lastFrameScoutedByUs = With.frame
    }
    if (base.ourUnits.exists(u => u.unitClass.isBuilding && u.visibleToOpponents)) {
      base.lastFrameScoutedByEnemy = With.frame
    }

    updateOwner(base)
  }
  
  private def updateOwner(base: Base): Unit = {
    val previousOwner = base.owner
    
    // Derive the owner from the current town hall
    // If we have previously inferred the base's owner, maintain the inference
    if (base.townHall.exists(_.player != base.owner)) {
      val hall = base.townHall.get
      With.logger.debug(f"Detecting ownership of base from visible town hall: $hall")
      base.owner = hall.player
    } else {
      val scoutingNow = base.lastFrameScoutedByUs == With.frame
      val framesSinceScouting = With.framesSince(base.lastFrameScoutedByUs)
      val hiddenNaturalDelay = Minutes(3)()
      val enemyCanAffordBase = With.sense.enemySecretMinerals >= With.enemy.townHallClass.mineralPrice

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

        if (framesSinceScouting > hiddenNaturalDelay && enemyCanAffordBase) {
          val building = base.zone.units.find(u => u.isEnemy && ! u.flying && u.unitClass.isBuilding)
          if (building.isDefined && With.frame > base.lastFrameScoutedByUs + hiddenNaturalDelay) {
            base.owner = building.get.player
            With.logger.debug(f"Assuming ${base.owner} owns $base last scouted at ${base.lastFrameScoutedByUs} due to presence of $building")
          }
        }

        if ( ! With.self.isZerg
          && With.scouting.weExpandedFirst
          && With.scouting.enemyNaturalPossiblyMining
          && base.naturalOf.exists(With.scouting.enemyMain.contains)
          && With.frame > Math.max(With.scouting.firstExpansionFrameUs, base.lastFrameScoutedByUs) + hiddenNaturalDelay) {
          base.owner = base.naturalOf.get.owner
          With.logger.debug(f"Assuming ${base.owner} has taken $base last scouted at ${base.lastFrameScoutedByUs} after we expanded on ${With.scouting.firstExpansionFrameUs}")
        }

        if ( ! base.scoutedByUs && base.owner.bases.forall(base.natural.contains) && (enemyCanAffordBase || With.scouting.enemyMain.isEmpty)) {
          base.natural.filter(_.isEnemy).foreach(natural => {
            base.owner = natural.owner
            With.logger.debug(f"Assuming ${base.owner} owns unscouted main $base due to possession of its natural {$base.natural}")
          })
        }
      }
    }

    if (base.owner != previousOwner) {
      base.frameTaken = Math.max(0, base.townHall.map(u => u.frameDiscovered - u.unitClass.buildFrames + (if (u.isAny(Zerg.Lair, Zerg.Hive)) 0 else u.remainingCompletionFrames)).getOrElse(With.frame))
      base.allTimeOwners += base.owner
    }
  }
}
