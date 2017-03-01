package Global.Combat.Battle

import Geometry.Positions
import Types.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleGroup(val units:mutable.HashSet[UnitInfo]) {
  
  var strength = 0
  var expectedSpread = 0
  var spread = 0
  var vanguard = Positions.middle
  var center = Positions.middle
}
