package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClasses.UnitClass

class PumpMatchingRatio(
  unitClass     : UnitClass,
  minimum       : Int,
  maximum       : Int,
  ratios        : Seq[MatchingRatio])
  extends Pump(unitClass) {
  
  description.set("Train " + unitClass + " based onratios")
  
  override def maxDesirable: Int = {
    Math.max(minimum, Math.min(maximum, Math.ceil(ratios.map(_.quantity).sum).toInt))
  }
}

trait MatchingRatio { def quantity: Double }

case class Enemy(enemyMatcher: UnitMatcher, ratio: Double = 1.0) extends MatchingRatio {
  def quantity: Double = With.units.countEnemy(enemyMatcher) * ratio
}
case class Friendly(enemyMatcher: UnitMatcher, ratio: Double = 1.0) extends MatchingRatio {
  def quantity: Double = With.units.countOurs(enemyMatcher) * ratio
}
