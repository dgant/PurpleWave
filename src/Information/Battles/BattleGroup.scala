package Information.Battles

import Mathematics.Positions.Positions
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Position

class BattleGroup(val units:Set[UnitInfo]) {
  
  var vanguard:Position = Positions.middle
  var center:Position = Positions.middle
  
  var strength      : Double = 0.0
  var desireToFight : Double = 1.0
}
