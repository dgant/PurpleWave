package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClasses.UnitClass

class TrainMatchingRatio(
  unitClass     : UnitClass,
  minimum       : Int,
  maximum       : Int,
  ratios        : Seq[MatchingRatio])
  extends TrainContinuously(unitClass) {
  
  description.set("Train " + unitClass + " based on enemies")
  
  override def maxDesirable: Int = {
    Math.max(minimum, Math.min(maximum, Math.ceil(ratios.map(_.quantity).sum).toInt))
  }
}

case class MatchingRatio (enemyMatcher: UnitMatcher,  ratio: Double) {
  def quantity: Double = With.units.countEnemy(enemyMatcher) * ratio
}
