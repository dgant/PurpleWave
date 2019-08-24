package Planning.Plans.Macro.Expanding

import Lifecycle.With

class MatchMiningBases(delta: Int = 0) extends RequireMiningBases {
  
  override def onUpdate() {
    basesDesired.set(delta + With.geography.enemyBases.count(_.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut))
    
    super.onUpdate()
  }
  
}
