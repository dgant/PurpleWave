package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpsEnemyAirNormal extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpsEnemyAirNormal.update()
  
}
