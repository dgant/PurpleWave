package Performance.Systems

import Lifecycle.{Manners, With}

class SystemManners extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyManners
  
  override protected def onRun() {
    Manners.run()
  }
}
