package Planning.Plans.Army

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountBetween
import Planning.UnitMatchers.{MatchWarriors, MatchWorkers}
import Planning.{Prioritized, Property}

class DefendAgainstWorkerRush extends Prioritized {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.matcher.set(MatchWorkers)
  
  def update() {
    val attackingWorkers = With.geography.ourBases
      .flatMap(_.units.filter(u =>
        u.isEnemy
        && u.unitClass.isWorker
        && u.matchups.targets.exists(ally =>
          (ally.unitClass.isBuilding || ally.friendly.exists(_.agent.toBuild.isDefined))
          && u.framesToGetInRange(ally) < 24 * 3)))
      .distinct
  
    lazy val attackingCentroid = PurpleMath.centroid(attackingWorkers.map(_.pixel))
    lazy val ourWorkers = With.units.countOurs(MatchWorkers)
    lazy val ourCombatUnits = With.units.countOurs(MatchWarriors)
    
    if (attackingWorkers.size < 3 || ourCombatUnits >= 3) return
      
    val workersToDefend = attackingWorkers.size + 3 - ourCombatUnits * 2
    if (defenders.get.units.size > workersToDefend) {
      defenders.get.release()
    }
    defenders.get.counter.set(new CountBetween(0, workersToDefend))
    defenders.get.acquire(this)
    defenders.get.units.foreach(unit => unit.agent.intend(this, new Intention {
      canFlee   = false
      toTravel  = Some(attackingCentroid)
    }))
  }
}
