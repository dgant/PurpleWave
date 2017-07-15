package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.UnitInfo

class Team(val units: Vector[UnitInfo]) {
  
  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle    : Battle      = _
  var vanguard  : Pixel       = SpecificPoints.middle
  var centroid  : Pixel       = SpecificPoints.middle
  
  //////////////
  // Features //
  //////////////
  
  def opponent: Team = if (battle.us == this) battle.enemy else battle.us
}
