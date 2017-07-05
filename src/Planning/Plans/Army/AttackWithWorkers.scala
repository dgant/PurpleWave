package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
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
  
  override def onUpdate() {
    fighters.acquire(this)
    if (With.geography.enemyBases.isEmpty) {
      findBases()
    }
    else {
      smorc()
    }
  }
  
  def findBases() {
    
    // Distribute scouts among unscouted bases, preferring to send more to the closest bases
    val unscoutedBases =
      With.geography.bases
        .toVector
        .sortBy( - _.heart.groundPixels(With.geography.home))
        .sortBy(_.isStartLocation)
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
    
    scoutAssignments.foreach(pair => smorc(pair._1, pair._2))
  }
  
  def smorc() {
    val base = With.geography.enemyBases.maxBy(_.workers.size)
    fighters.units.foreach(smorc(_, base))
  }
  
  def smorc(unit: FriendlyUnitInfo, base: Base) {
    With.executor.intend(new Intention(this, unit) {
      toTravel = Some(base.heart.pixelCenter)
      smorc = true
    })
  }
}
