package Performance.Tasks.Global

import Lifecycle.{Manners, With}
import Performance.Tasks.AbstractTask

class TaskManners extends AbstractTask {
  
  override def urgency: Int = With.configuration.urgencyManners
  
  override protected def onRun() {
    Manners.run()
  }
}
