package Mathematics.Formations

import Mathematics.Pixels.Pixel
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class FormationParticipant(unit: UnitInfo) {
  val unitClass   : UnitClass = unit.unitClass
  val pixelBefore : Pixel     = unit.pixelCenter
  val range       : Double    = unit.pixelRangeMax.toInt + unit.unitClass.radialHypotenuse
  
  var pixelAfter  : Pixel     = _
}
