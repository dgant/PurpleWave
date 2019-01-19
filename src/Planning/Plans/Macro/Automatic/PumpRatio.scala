package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClasses.UnitClass

class PumpRatio(
  unitClass     : UnitClass,
  minimum       : Int,
  maximum       : Int,
  ratios        : Seq[MatchingRatio])
  extends Pump(unitClass) {
  
  description.set("Train " + unitClass + " based on ratios")
  
  override def maxDesirable: Int = PurpleMath.clamp(Math.ceil(ratios.map(_.quantity).sum).toInt, minimum, maximum)
}

trait MatchingRatio { def quantity: Double }

case class Flat(value: Double) extends MatchingRatio {
  def quantity: Double = value
}
case class Enemy(enemyMatcher: UnitMatcher, ratio: Double = 1.0) extends MatchingRatio {
  def quantity: Double = With.units.countEnemy(enemyMatcher) * ratio
}
case class Friendly(unitMatcher: UnitMatcher, ratio: Double = 1.0) extends MatchingRatio {
  def quantity: Double = With.units.countOurs(unitMatcher) * ratio
}
