package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountBetween, UnitCountExactly}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchNotHoldingResources, UnitMatchWorkers}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}

import scala.collection.mutable

class Scout(scoutCount: Int = 1) extends Plan {
  
  description.set("Scout")
  
  var useScoutingBehaviors = true
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchAnd(UnitMatchWorkers, UnitMatchNotHoldingResources))
    unitPreference.set(UnitPreferClose(SpecificPoints.middle))
    interruptable.set(false)
  })
  
  var acquiredScouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  
  override def isComplete: Boolean = {
    val bases = With.geography.enemyBases
    if (With.units.countOurs(UnitMatchWorkers) < 3)                     return true
    if (bases.isEmpty)                                                  return false
    if (With.blackboard.lastScoutDeath > 0)                             return true
    if (bases.exists(_.townHall.isDefined) && scouts.get.units.isEmpty) return true
    // With 4Pool use the scout to help harass/distract
    if ( ! ZvE4Pool.active && ! ZvT1HatchHydra.active && bases.exists(_.units.exists(u => u.unitClass.isStaticDefense && u.complete))) return true
    if ( ! ZvE4Pool.active && ! ZvT1HatchHydra.active && With.geography.enemyBases.exists(_.units.exists(u => u.isOurs && ! scouts.get.unitMatcher.get.accept(u)))) return true
    false
  }
  
  override def onUpdate() {
    if (isComplete) return

    scouts.get.unitCounter.set(UnitCountExactly(PurpleMath.clamp(scoutCount, 1, With.geography.startBases.count(_.scouted))))
    
    val scoutsDied = acquiredScouts.exists( ! _.alive)
    if (scoutsDied) {
      acquiredScouts = List.empty
      With.blackboard.lastScoutDeath = With.frame
    }
    if (With.framesSince(With.blackboard.lastScoutDeath) < 24 * 30) {
      return
    }
  
    val enemyStartBases = With.geography.startBases.filter(_.owner.isEnemy)
    val scoutsDesired: Int = if (With.geography.enemyBases.nonEmpty) 1 else Math.min(scoutCount, With.geography.startLocations.size - 1)
    
    val getNextScoutBase = () => {
      if (enemyStartBases.isEmpty) {
        With.scouting.claimBaseToScout()
      }
      else {
        enemyStartBases.head
      }
    }
    
    if (acquiredScouts.size > scoutsDesired) {
      scouts.get.release()
    }
    
    scouts.get.unitCounter.set(new UnitCountBetween(1, scoutsDesired))
    scouts.get.unitPreference.set(UnitPreferClose(With.scouting.mostIntriguingBases().head.heart.pixelCenter))
    scouts.get.acquire(this)
    acquiredScouts = scouts.get.units
    
    val unassignedScouts = new mutable.ListBuffer[FriendlyUnitInfo]
    unassignedScouts ++= acquiredScouts
    
    while (unassignedScouts.nonEmpty) {
      val destination = With.scouting.claimBaseToScout().heart.pixelCenter
      val scout = unassignedScouts.minBy(scout => scout.pixelDistanceTravelling(destination) + 0.5 * scout.pixelDistanceTravelling(scout.agent.destination, destination))
      unassignedScouts -= scout
      
      With.scouting.highlightScout(scout)
      
      val intention = new Intention
      intention.toTravel = Some(destination)
      intention.canScout = useScoutingBehaviors
      scout.agent.intend(this, intention)
    }
  }
}
