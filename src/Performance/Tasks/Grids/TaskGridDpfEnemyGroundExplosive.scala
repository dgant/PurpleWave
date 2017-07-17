package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyGroundExplosive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyGroundExplosive.update()
  
}
