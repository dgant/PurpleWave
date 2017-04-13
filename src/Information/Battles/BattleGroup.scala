package Information.Battles

import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units:Set[UnitInfo]) {
  
  var vanguard:Pixel = Points.middle
  var center:Pixel = Points.middle
  
  var strength      : Double = 0.0
}
