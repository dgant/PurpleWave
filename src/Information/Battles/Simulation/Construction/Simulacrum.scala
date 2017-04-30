package Information.Battles.Simulation.Construction

import Lifecycle.With
import Mathematics.Pixels.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class Simulacrum(val unit:UnitInfo) {
  
  val flying            : Boolean   = unit.flying
  val topSpeed          : Double    = unit.topSpeed
  val attacksAir        : Boolean   = unit.unitClass.attacksAir
  val attacksGround     : Boolean   = unit.unitClass.attacksGround
  val rangeAir          : Double    = unit.unitClass.airRange
  val rangeGround       : Double    = unit.unitClass.groundRange
  val radialHypotenuse  : Double    = unit.unitClass.radialHypotenuse
  val woundedThreshold  : Int       = Math.min(With.configuration.woundedThreshold, unit.unitClass.maxTotalHealth / 3)
  
  var target: Option[Simulacrum] = None
  var threat: Option[Simulacrum] = None
  
  var damageTaken       : Int       = 0
  var pixel             : Pixel     = unit.pixelCenter
  var hitPoints         : Int       = unit.hitPoints
  var shields           : Int       = unit.shieldPoints
  var attackCooldown    : Int       = unit.cooldownLeft
  var moveCooldown      : Int       = Math.min(8, unit.cooldownLeft) //Rough approximation
  
  var fleeing   : Boolean = false
  var fighting  : Boolean = unit.unitClass.helpsInCombat
  var ignoring  : Boolean = false
  
  def totalLife     : Int     = hitPoints + shields
  def alive         : Boolean = totalLife > 0
  def readyToAttack : Boolean = attackCooldown == 0
  def readyToMove   : Boolean = canMove && moveCooldown == 0
  
  // This logic is duplicated from other sources because this version is much faster
  def canMove                             : Boolean = topSpeed > 0
  def canAttack       (enemy:Simulacrum)  : Boolean = if (enemy.flying) attacksAir else attacksGround
  def rangeAgainst    (enemy:Simulacrum)  : Double  = (if (enemy.flying) rangeAir else rangeGround) - radialHypotenuse - enemy.radialHypotenuse
  def inRangeToAttack (enemy:Simulacrum)  : Boolean = rangeAgainst(enemy) * rangeAgainst(enemy) >= pixel.pixelDistanceSquared(enemy.pixel)

  override def toString:String = (
    (if (unit.player == With.self) "our " else "")
    + unit.unitClass.toString
    + " #"
    + unit.id
    + (if (alive) " (" + totalLife + "/" + unit.unitClass.maxTotalHealth + ")" else " (DEAD)")
    + (if (fleeing) " (Fleeing)" else "")
    + (if (ignoring) " (Ignoring)" else "")
    )
}
