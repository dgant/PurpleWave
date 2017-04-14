package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridEnemyVision extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.enemyVision.update()
  
}
