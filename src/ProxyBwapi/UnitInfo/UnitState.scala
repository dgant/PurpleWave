package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitClass.UnitClass

class UnitState(unit: UnitInfo) {
  val frame                 : Int               = With.frame
  val pixelCenter           : Pixel             = unit.pixelCenter
  val velocitySquared       : Double            = unit.velocityX * unit.velocityX + unit.velocityY * unit.velocityY
  val attackStarting        : Boolean           = unit.attackStarting
  val hitPoints             : Int               = unit.hitPoints
  val shieldPoints          : Int               = unit.shieldPoints
  val defensiveMatrixPoints : Int               = unit.defensiveMatrixPoints
  val couldMoveThisFrame    : Boolean           = unit.canMove
  val couldAttackThisFrame  : Boolean           = unit.readyForAttackOrder
  val cooldown              : Int               = unit.cooldownLeft
  val unitClass             : UnitClass         = unit.unitClass
  val attackTarget          : Option[UnitInfo]  = unit.target.filter(_.isEnemyOf(unit))
  var tryingToMove          : Boolean           = unit.targetPixel.exists(_.pixelDistanceFast(unit.pixelCenter) > 32.0)
  
  def tryingToAttack: Boolean = attackTarget.isDefined
  def age: Int = With.frame - frame
}
