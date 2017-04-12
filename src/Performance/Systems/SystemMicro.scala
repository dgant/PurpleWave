package Performance.Systems

import Lifecycle.With

class SystemMicro extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyMicro
  
  override protected def onRun(): Unit = {
    With.commander.run()
    With.executor.run()
  }
}
