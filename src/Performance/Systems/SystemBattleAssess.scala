package Performance.Systems

import Information.Battles.BattleUpdater
import Lifecycle.With

class SystemBattleAssess extends AbstractSystem {
  
  override def urgency: Int = With.configuration.urgencyBattles
  
  override protected def onRun() {
    BattleUpdater.run()
  }
}
