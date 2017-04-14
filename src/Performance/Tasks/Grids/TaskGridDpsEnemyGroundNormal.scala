package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyGroundNormal extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyGroundNormal.update()
  
}
