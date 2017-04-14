package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridPsi2x2and3x2 extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.psi2x2and3x2.update()
  
}
