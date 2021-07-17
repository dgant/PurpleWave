package Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchWarriors, MatchWorker}

class DefendAgainstWorkerRush extends Tactic {
  
  val defenders = new LockUnits(this)
  defenders.matcher = MatchWorker
  
  def launch() {
    val attackingWorkers = With.geography.ourBases
      .flatMap(_.units.filter(u =>
        u.isEnemy
        && u.unitClass.isWorker
        && u.matchups.targets.exists(ally =>
          (ally.unitClass.isBuilding || ally.friendly.exists(_.intent.toBuild.isDefined))
          && u.framesToGetInRange(ally) < 24 * 3)))
      .distinct
  
    lazy val attackingCentroid = Maff.centroid(attackingWorkers.map(_.pixel))
    lazy val ourWorkers = With.units.countOurs(MatchWorker)
    lazy val ourCombatUnits = With.units.countOurs(MatchWarriors)
    
    if (attackingWorkers.size < 3 || ourCombatUnits >= 3) return
      
    val workersToDefend = attackingWorkers.size + 3 - ourCombatUnits * 2
    if (defenders.units.size > workersToDefend) {
      defenders.release()
    }
    defenders.counter = CountUpTo(workersToDefend)
    defenders.acquire(this)
    defenders.units.foreach(unit => unit.intend(this, new Intention {
      canFlee   = false
      toTravel  = Some(attackingCentroid)
    }))
  }
}
