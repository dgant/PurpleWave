package Performance.Tasks.Global

import Lifecycle.{Manners, With}
import Performance.Tasks.AbstractTask

class TaskManners extends AbstractTask {
  
  urgency = With.configuration.urgencyManners
  
  override protected def onRun() {
    Manners.run()
  }
}
