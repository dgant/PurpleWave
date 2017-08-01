package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClass.UnitClass

class TrainMatchingRatio(
  unitClass     : UnitClass,
  maximum       : Int,
  ratios        : Seq[MatchingRatio])
  extends TrainContinuously(unitClass) {
  
  description.set("Train " + unitClass + " based on enemies")
  
  override def maxDesirable: Int = {
    Math.min(maximum, Math.ceil(ratios.map(_.quantity).sum).toInt)
  }
}

case class MatchingRatio (enemyMatcher: UnitMatcher,  ratio: Double) {
  def quantity: Double = With.units.enemy.count(enemyMatcher.accept) * ratio
}
