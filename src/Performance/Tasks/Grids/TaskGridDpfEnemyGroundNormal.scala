package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyGroundNormal extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyGroundNormal.update()
  
}
