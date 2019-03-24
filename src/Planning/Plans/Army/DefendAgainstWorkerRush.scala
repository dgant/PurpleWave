package Planning.Plans.Army

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import Planning.{Plan, Property}

class DefendAgainstWorkerRush extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    val attackingWorkers = With.geography.ourBases
      .flatMap(_.units.filter(u =>
        u.isEnemy
        && u.unitClass.isWorker
        && u.matchups.targets.exists(ally =>
          (ally.unitClass.isBuilding || ally.friendly.exists(_.agent.toBuild.isDefined))
          && u.framesToGetInRange(ally) < 24 * 3)))
      .distinct
  
    lazy val attackingCentroid = PurpleMath.centroid(attackingWorkers.map(_.pixelCenter))
    lazy val ourWorkers = With.units.countOurs(UnitMatchWorkers)
    lazy val ourCombatUnits = With.units.countOurs(UnitMatchWarriors)
    
    if (attackingWorkers.size < 3 || ourCombatUnits >= 3) return
      
    var workersToDefend = attackingWorkers.size + 3 - ourCombatUnits * 2
    if (defenders.get.units.size > workersToDefend) {
      defenders.get.release()
    }
    defenders.get.unitCounter.set(new UnitCountBetween(0, workersToDefend))
    defenders.get.acquire(this)
    defenders.get.units.foreach(unit => unit.agent.intend(this, new Intention {
      canFlee   = false
      toTravel  = Some(attackingCentroid)
    }))
  }
}
