package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyAirNormal extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyAirNormal.update()
  
}
