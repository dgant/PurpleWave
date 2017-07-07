package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plans.Army.DefendChokes

class DefendChokeWithWorkers extends DefendChokes {
  override def onUpdate(): Unit = {
    defenders.get.unitMatcher.set(UnitMatchOr(UnitMatchWorkers, UnitMatchWarriors))
    defenders.get.unitCounter.set(UnitCountExactly(With.units.ours.count(_.unitClass.isWorker) / 2))
    super.onUpdate()
  }
}
