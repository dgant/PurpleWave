package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.Plans.Macro.Automatic.Rounding.Rounding
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitClasses.UnitClass


object Rounding extends Enumeration {
  type Rounding = Value
  val Up, Down, Round = Value
}

class PumpRatio(
  unitClass     : UnitClass,
  minimum       : Int,
  maximum       : Int,
  ratios        : Seq[MatchingRatio],
  round         : Rounding = Rounding.Up)
  extends Pump(unitClass) {

  
  description.set("Train " + unitClass + " based on ratios")
  
  override def maxDesirable: Int = PurpleMath.clamp(
    (round match {
      case Rounding.Up => x: Double => Math.ceil(x)
      case Rounding.Down => x: Double => Math.floor(x)
      case Rounding.Round => x: Double => Math.round(x).toDouble
    })(ratios.map(_.quantity).sum).toInt,
    minimum,
    maximum)
}

trait MatchingRatio { def quantity: Double }

case class Flat(value: Double) extends MatchingRatio {
  def quantity: Double = value
}
case class Enemy(enemyMatcher: UnitMatcher, ratio: Double) extends MatchingRatio {
  def quantity: Double = With.units.countEnemy(enemyMatcher) * ratio
}
case class Friendly(unitMatcher: UnitMatcher, ratio: Double) extends MatchingRatio {
  def quantity: Double = With.units.countOurs(unitMatcher) * ratio
}
