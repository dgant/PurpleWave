package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plan
import Utilities.EnrichPixel._

class DefendAgainstWorkerRush extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    val attackingWorkers = With.geography.ourBases
      .flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isWorker))
      .toSet
  
    lazy val attackingCentroid = attackingWorkers.map(_.pixelCenter).centroid
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
