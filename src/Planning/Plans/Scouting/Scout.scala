package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchNotHoldingResources, UnitMatchWorkers}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Scout(scoutCount: Int = 1) extends Plan {
  
  description.set("Scout")
  
  var useScoutingBehaviors = true
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(UnitCountExactly(scoutCount))
    unitMatcher.set(UnitMatchAnd(UnitMatchWorkers, UnitMatchNotHoldingResources))
    unitPreference.set(UnitPreferClose(SpecificPoints.middle))
    interruptable.set(false)
    canPoach.set(true)
  })
  
  var acquiredScouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  
  override def isComplete: Boolean = {
    val bases = With.geography.enemyBases
    if (With.units.countOurs(UnitMatchWorkers) < 3)                     return true
    if (bases.isEmpty)                                                  return false
    if (With.blackboard.lastScoutDeath > 0)                             return true
    if (bases.exists(_.zone.walledIn))                                  return true
    if (bases.exists(_.units.exists(_.unitClass.isStaticDefense)))      return true
    if (bases.exists(_.townHall.isDefined) && scouts.get.units.isEmpty) return true
    if (With.geography.enemyBases.exists(_.units.exists(u => u.isOurs && ! u.unitClass.isWorker))) return true
    false
  }
  
  override def onUpdate() {
    if (isComplete) return
    
    val scoutsDied = acquiredScouts.nonEmpty && acquiredScouts.exists( ! _.alive)
    if (scoutsDied) {
      acquiredScouts = List.empty
      With.blackboard.lastScoutDeath = With.frame
    }
    if (With.framesSince(With.blackboard.lastScoutDeath) < 24 * 30) {
      return
    }
  
    val enemyStartBases = With.geography.startBases.filter(_.owner.isEnemy)
    var scoutsDesired: Int = 1
    
    val getNextScoutBase = () => {
      if (enemyStartBases.isEmpty) {
        With.intelligence.dequeueNextBaseToScout
      }
      else {
        enemyStartBases.head
      }
    }
    
    if (acquiredScouts.size > scoutsDesired) {
      scouts.get.release()
    }
    
    scouts.get.unitCounter.set(UnitCountExactly(scoutsDesired))
    scouts.get.unitPreference.set(UnitPreferClose(With.intelligence.leastScoutedBases.head.heart.pixelCenter))
    scouts.get.acquire(this)
    acquiredScouts = scouts.get.units
    
    val unassignedScouts = new mutable.ListBuffer[FriendlyUnitInfo]
    unassignedScouts ++= acquiredScouts
    
    while (unassignedScouts.nonEmpty) {
      val destination = With.intelligence.dequeueNextBaseToScout.heart.pixelCenter
      val scout = unassignedScouts.minBy(_.pixelDistanceTravelling(destination))
      unassignedScouts -= scout
      
      With.intelligence.highlightScout(scout)
      
      val intention = new Intention
      intention.toTravel = Some(destination)
      intention.canScout = useScoutingBehaviors
      scout.agent.intend(this, intention)
    }
  }
}
