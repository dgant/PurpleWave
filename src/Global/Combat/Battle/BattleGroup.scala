package Global.Combat.Battle

import Geometry.Positions
import Types.UnitInfo.UnitInfo
import bwapi.Position

import scala.collection.mutable

class BattleGroup(
  var vanguard:Position,
  val units:mutable.HashSet[UnitInfo]) {
  
  var strength = 0
  var expectedSpread = 0
  var spread = 0
  var center = Positions.middle
}
