package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridEnemyDetection extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.enemyDetection.update()
  
}
