package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyAirExplosive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyAirExplosive.update()
  
}
