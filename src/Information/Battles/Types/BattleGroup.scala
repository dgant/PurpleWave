package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units: Vector[UnitInfo]) {
  
  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle    : Battle      = _
  var vanguard  : Pixel       = SpecificPoints.middle
  var centroid  : Pixel       = SpecificPoints.middle
  
  //////////////
  // Features //
  //////////////
  
  def opponent: BattleGroup = if (battle.us == this) battle.us else battle.enemy
}
