package Macro.Decisions

import ProxyBwapi.UnitClass.UnitClass

case class RatioOurs(
  unit      : UnitClass,
  otherUnit : UnitClass,
  ratio     : Double)
    extends Ratio {
  
  protected def quantity(context: DesireContext): Int = context.ourUnits.getOrElse(otherUnit, 0)
}