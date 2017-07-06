package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.DefendChokes

class BlockWithWorkers extends DefendChokes {
  override def onUpdate(): Unit = {
    defenders.get.unitMatcher.set(UnitMatchWorkers)
    defenders.get.unitCounter.set(new UnitCountExactly(With.units.ours.count(_.unitClass.isWorker) / 2))
    super.onUpdate()
  }
}
