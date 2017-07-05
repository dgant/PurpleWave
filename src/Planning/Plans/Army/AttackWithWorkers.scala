package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class AttackWithWorkers extends Plan {
  
  val fighters = new LockUnits
  fighters.unitMatcher.set(UnitMatchWorkers)
  fighters.unitCounter.set(UnitCountEverything)
  
  var haveSeenABase = false
  
  override def onUpdate() {
    fighters.acquire(this)
    if (With.geography.enemyBases.isEmpty && ! With.units.enemy.exists(unit => unit.unitClass.isBuilding && ! unit.flying)) {
      findBases()
    }
    else {
      haveSeenABase = true
      smorc()
    }
  }
  
  def findBases() {
    
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases
    val unscoutedBases =
      With.geography.bases
        .filter( ! _.owner.isUs)
        .filter(_.isStartLocation || haveSeenABase) //Only search non-start locations until we've killed the first
        .toVector
        .sortBy( - _.heart.groundPixels(With.geography.home))
        .sortBy( ! _.isStartLocation)
        .sortBy(_.lastScoutedFrame)
        
  
    val unassignedScouts = new mutable.HashSet[FriendlyUnitInfo] ++ fighters.units
    val scoutAssignments = new mutable.HashMap[FriendlyUnitInfo, Base]
    while(unassignedScouts.nonEmpty) {
      unscoutedBases.foreach(base => {
        if (unassignedScouts.nonEmpty) {
          val scout = unassignedScouts.minBy(_.pixelDistanceTravelling(base.heart))
          unassignedScouts.remove(scout)
          scoutAssignments(scout) = base
        }
      })
    }
    
    scoutAssignments.foreach(pair => smorc(pair._1, pair._2.heart.pixelCenter))
  }
  
  def smorc() {
    val base = With.geography.enemyBases.toList.sortBy(_.workers.size).lastOption
    val target = base.map(_.heart.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    fighters.units.foreach(smorc(_, target))
  }
  
  def smorc(unit: FriendlyUnitInfo, target: Pixel) {
    With.executor.intend(new Intention(this, unit) {
      toTravel = Some(target)
      smorc = true
    })
  }
}
