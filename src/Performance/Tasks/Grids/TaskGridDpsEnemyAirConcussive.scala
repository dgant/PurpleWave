package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyAirConcussive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyAirConcussive.update()
  
}
