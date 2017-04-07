package Information.Battles.Simulator

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Position

class Simulacrum(val unit:UnitInfo) {
  var pixel          : Position = unit.pixelCenter
  var hitPoints      : Int      = unit.hitPoints
  var shields        : Int      = unit.shieldPoints
  var attackCooldown : Int      = unit.cooldownLeft
  var moveCooldown   : Int      = Math.min(8, unit.cooldownLeft) //Rough approximation
  
  def totalLife : Int     = hitPoints + shields
  def alive     : Boolean = totalLife > 0
}
