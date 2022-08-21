package Tactic.Tactics.WorkerPulls

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitFilters.{IsWarrior, IsWorker}

class PullVsWorkerRush extends WorkerPull {
  lazy val attackingWorkers = With.geography.ourBases
    .flatMap(_.enemies.filter(u =>
      u.unitClass.isWorker
        && u.matchups.targets.exists(ally =>
        (ally.unitClass.isBuilding || ally.friendly.exists(_.intent.toBuild.isDefined))
          && u.framesToGetInRange(ally) < 24 * 3)))
    .distinct

  lazy val attackingCentroid = Maff.centroid(attackingWorkers.map(_.pixel))
  lazy val ourWorkers = With.units.countOurs(IsWorker)
  lazy val ourCombatUnits = With.units.countOurs(IsWarrior)
  lazy val workersToDefend = if (skip) 0 else attackingWorkers.size + 3 - ourCombatUnits * 2

  var skip = false
  skip || ourCombatUnits >= 3
  skip ||= attackingWorkers.size < 3

  override def apply(): Int = workersToDefend

  override def employ(defenders: Seq[FriendlyUnitInfo]): Unit = {
    defenders.foreach(unit => unit.intend(this, new Intention {
      canFlee   = false
      toTravel  = Some(attackingCentroid)
    }))
  }
}
