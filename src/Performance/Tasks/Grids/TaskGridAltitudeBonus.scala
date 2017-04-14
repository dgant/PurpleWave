package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridAltitudeBonus extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.altitudeBonus.update()
  
}
