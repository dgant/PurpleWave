package Performance.Systems

import Lifecycle.With

class SystemBattleClassify extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyBattles
  
  override protected def onRun() {
    With.battles.classify()
  }
}
