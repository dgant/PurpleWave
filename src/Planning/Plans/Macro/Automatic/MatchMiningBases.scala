package Planning.Plans.Macro.Automatic

import Lifecycle.With

class MatchMiningBases(delta: Int = 0) extends BuildMiningBases {
  
  override def onUpdate() {
    basesDesired.set(delta + With.geography.enemyBases.count(_.mineralsLeft > With.configuration.maxMineralsBeforeMinedOut))
    
    super.onUpdate()
  }
  
}
