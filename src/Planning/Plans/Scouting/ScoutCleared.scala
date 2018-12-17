package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Predicate

class ScoutCleared extends Predicate {
  override def isComplete: Boolean = ! With.geography.ourMain.units.exists(u => u.isEnemy && u.unitClass.isWorker)
}
