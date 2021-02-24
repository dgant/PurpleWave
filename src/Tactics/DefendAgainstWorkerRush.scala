package Tactics

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchWarriors, MatchWorkers}

class DefendAgainstWorkerRush extends Prioritized {
  
  val defenders = new LockUnits
  defenders.matcher.set(MatchWorkers)
  
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
    if (defenders.units.size > workersToDefend) {
      defenders.release()
    }
    defenders.counter.set(CountUpTo(workersToDefend))
    defenders.acquire(this)
    defenders.units.foreach(unit => unit.agent.intend(this, new Intention {
      canFlee   = false
      toTravel  = Some(attackingCentroid)
    }))
  }
}
