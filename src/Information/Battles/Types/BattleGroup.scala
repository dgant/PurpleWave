package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units:Vector[UnitInfo]) {
  
  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle    : Battle      = _
  var opponent  : BattleGroup = _
  var vanguard  : Pixel       = SpecificPoints.middle
  var centroid  : Pixel       = SpecificPoints.middle
  var spread    : Double      = _
  
  //////////////
  // Features //
  //////////////
}
