package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridPsi4x3 extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.psi3Height.update()
  
}
