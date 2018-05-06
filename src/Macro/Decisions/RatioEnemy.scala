package Macro.Decisions

import ProxyBwapi.UnitClasses.UnitClass

case class RatioEnemy(
  unit      : UnitClass,
  otherUnit : UnitClass,
  ratio     : Double)
    extends Ratio {
    
  protected def quantity(context: DesireContext): Int = context.enemyUnits.getOrElse(otherUnit, 0)
}