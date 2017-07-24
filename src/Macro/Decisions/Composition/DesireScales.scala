package Macro.Decisions.Composition

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass

object DesireScales {
  
  val default = new DesireScale
  
  val observer = new DesireScale(1.0, 4.0)
  
  val reaver = new DesireScale(1.0, 6.0)
  
  def forUnit(unit: UnitClass): DesireScale = {
    if (unit == Protoss.Observer)
      observer
    else if (unit == Protoss.Reaver)
      reaver
    else
      default
  }
}
