package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyAirExplosive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyAirExplosive.update()
  
}
