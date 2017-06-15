package ProxyBwapi.UnitInfo

import Lifecycle.With

class UnitState(unit: UnitInfo) {
  var frame                 : Int = With.frame
  var hitPoints             : Int = unit.hitPoints
  var shieldPoints          : Int = unit.shieldPoints
  var defensiveMatrixPoints : Int = unit.defensiveMatrixPoints
  
  def age: Int = With.frame - frame
}
