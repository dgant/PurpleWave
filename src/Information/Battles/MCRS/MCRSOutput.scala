package Information.Battles.MCRS

import ProxyBwapi.UnitInfo.UnitInfo

class MCRSOutput(unit: UnitInfo) {
  var attackAirAsAir: Double = 0.0
  var attackAirAsGround: Double = 0.0
  var attackGroundAsAir: Double = 0.0
  var attackGroundasGround: Double = 0.0
  var synchronizeAirAndGround: Boolean = false

  def simValue: Double =
    if (synchronizeAirAndGround) {
      if (unit.flying)
        Math.min(attackAirAsAir, attackGroundAsAir)
      else
        Math.min(attackAirAsGround, attackGroundasGround)
    }
    else if (unit.flying)
      if (unit.target.exists(_.flying)) attackAirAsAir else attackGroundAsAir
    else
      if (unit.target.exists(_.flying)) attackAirAsGround else attackGroundasGround
}
