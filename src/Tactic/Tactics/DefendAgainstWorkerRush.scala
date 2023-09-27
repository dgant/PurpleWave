package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class DefendAgainstWorkerRush extends Tactic {
  
  val defenders = new LockUnits(this, IsWorker)
  
  def launch(): Unit = {
    val attackingWorkers = With.geography.ourBases
      .flatMap(_.enemies.filter(u =>
        u.unitClass.isWorker
        && u.matchups.targets.exists(ally =>
          (ally.unitClass.isBuilding || ally.friendly.exists(_.intent.toBuild.isDefined))
          && u.framesToGetInRange(ally) < 24 * 3)))
      .distinct

    val ourCombatUnits = With.units.countOurs(IsWarrior)
    
    if (attackingWorkers.size < 3 || ourCombatUnits >= 3) return
      
    val workersToDefend = attackingWorkers.size + 3 - ourCombatUnits * 2
    if (defenders.units.size > workersToDefend) {
      defenders.release()
    }

    lazy val attackingCentroid = Maff.centroid(attackingWorkers.map(_.pixel))
    defenders
      .setCounter(CountUpTo(workersToDefend))
      .acquire()
      .foreach(_.intend(this)
        .setCanFlee(false)
        .setTerminus(attackingCentroid))
  }
}
