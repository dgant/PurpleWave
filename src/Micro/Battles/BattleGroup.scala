package Micro.Battles

import Geometry.Positions
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleGroup(val units:mutable.HashSet[UnitInfo]) {
  
  var strength = 0
  var vanguard = Positions.middle
  var center = Positions.middle
}
