package Macro.Actions

import Lifecycle.With
import Macro.Actions.Rounding.Rounding
import Mathematics.Maff
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.UnitFilters.UnitFilter

object Rounding extends Enumeration {
  type Rounding = Value
  val Up, Down, Round = Value
}

object PumpRatio {
  def apply(
    unitClass     : UnitClass,
    minimum       : Int,
    maximum       : Int,
    ratios        : Seq[MatchingRatio],
    round         : Rounding = Rounding.Up): Unit = {

    val maxDesirable: Int = Maff.clamp(
      (round match {
        case Rounding.Up => x: Double => Math.ceil(x)
        case Rounding.Down => x: Double => Math.floor(x)
        case Rounding.Round => x: Double => Math.round(x).toDouble
      })(ratios.map(_.quantity).sum).toInt,
      minimum,
      maximum)
  }
}

trait MatchingRatio { def quantity: Double }

case class Flat(value: Double) extends MatchingRatio {
  def quantity: Double = value
}
case class Enemy(enemyMatcher: UnitFilter, ratio: Double) extends MatchingRatio {
  def quantity: Double = With.units.countEnemy(enemyMatcher) * ratio
}
case class Friendly(unitMatcher: UnitFilter, ratio: Double) extends MatchingRatio {
  def quantity: Double = With.units.countOurs(unitMatcher) * ratio
}
