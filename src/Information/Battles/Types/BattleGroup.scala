package Information.Battles.Types

import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units:Vector[UnitInfo]) {
  
  // These should be populated immediately after construction.
  var battle    : Battle      = _
  var opponent  : BattleGroup = _
  var vanguard  : Pixel       = Points.middle
  var centroid  : Pixel       = Points.middle
  var spread    : Double      = _
}
