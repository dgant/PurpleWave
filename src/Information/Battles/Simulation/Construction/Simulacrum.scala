package Information.Battles.Simulation.Construction

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Position

class Simulacrum(val unit:UnitInfo) {
  
  val flying            : Boolean   = unit.flying
  val attacksAir        : Boolean   = unit.unitClass.attacksAir
  val attacksGround     : Boolean   = unit.unitClass.attacksGround
  val rangeAir          : Double    = unit.unitClass.airRange
  val rangeGround       : Double    = unit.unitClass.groundRange
  val radialHypotenuse  : Double    = unit.unitClass.radialHypotenuse
  
  var pixel             : Position = unit.pixelCenter
  var hitPoints         : Int      = unit.hitPoints
  var shields           : Int      = unit.shieldPoints
  var attackCooldown    : Int      = unit.cooldownLeft
  var moveCooldown      : Int      = Math.min(8, unit.cooldownLeft) //Rough approximation
  
  var fleeing   : Boolean = false
  var fighting  : Boolean = true
  
  def totalLife     : Int     = hitPoints + shields
  def alive         : Boolean = totalLife > 0
  def readyToAttack : Boolean = attackCooldown == 0
  def readyToMove   : Boolean = moveCooldown == 0
  
  // This logic is duplicated from other sources because this version is reallt fast
  def rangeAgainst  (enemy:Simulacrum)  : Double   = (if (enemy.flying) rangeAir else rangeGround) - radialHypotenuse - enemy.radialHypotenuse
  def canAttack     (enemy:Simulacrum)  : Boolean  = if (enemy.flying) attacksAir else attacksGround
}
