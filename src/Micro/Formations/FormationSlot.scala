package Micro.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class FormationSlot(unit: UnitInfo) {
  val unitClass     : UnitClass = unit.unitClass
  val idealDistance : Double    = unit.pixelRangeMax.toInt + unit.unitClass.radialHypotenuse
  
  var pixelAfter: Pixel = _
}
