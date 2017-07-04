package Planning.Plans.Information.Scenarios

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plan

class WeAreBeingWorkerRushed extends Plan {
  
  override def isComplete: Boolean = {
    ! With.units.ours.exists(UnitMatchWarriors.accept) &&
    ! With.units.enemy.exists(UnitMatchWarriors.accept) &&
    With.units.enemy.count(unit => unit.unitClass.isWorker && unit.pixelCenter.zone.bases.exists(_.owner.isUs)) >
      Math.min(2, With.units.ours.count(UnitMatchWorkers.accept))
  }
}
