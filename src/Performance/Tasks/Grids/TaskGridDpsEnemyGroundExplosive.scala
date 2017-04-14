package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyGroundExplosive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyGroundExplosive.update()
  
}
