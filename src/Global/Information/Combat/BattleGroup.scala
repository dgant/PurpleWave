package Global.Information.Combat

import Types.UnitInfo.UnitInfo
import bwapi.Position

import scala.collection.mutable

class BattleGroup(
  var vanguard:Position,
  val units:mutable.HashSet[UnitInfo]) {
  
}
