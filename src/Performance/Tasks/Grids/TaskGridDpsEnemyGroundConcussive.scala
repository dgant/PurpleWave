package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyGroundConcussive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyGroundConcussive.update()
  
}
