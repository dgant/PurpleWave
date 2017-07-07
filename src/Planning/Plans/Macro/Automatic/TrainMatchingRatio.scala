package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClass.UnitClass

class TrainMatchingRatio(
  unitClass     : UnitClass,
  enemyMatcher  : UnitMatcher,
  ratio         : Double,
  maximum       : Int = Int.MaxValue)
  extends TrainContinuously(unitClass) {
  
  description.set("Train " + unitClass + " at " + ratio + ":1 vs. matching enemy")
  
  override def maxDesirable: Int = {
    Math.min(maximum, Math.ceil(With.units.enemy.count(enemyMatcher.accept) * ratio).toInt)
  }
}
