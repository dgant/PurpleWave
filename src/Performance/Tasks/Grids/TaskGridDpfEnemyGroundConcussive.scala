package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyGroundConcussive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyGroundConcussive.update()
  
}
