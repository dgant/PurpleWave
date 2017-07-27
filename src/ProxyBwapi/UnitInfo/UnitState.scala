package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.Pixel
import bwapi.UnitCommandType

class UnitState(unit: UnitInfo) {
  val frame                 : Int     = With.frame
  val pixelCenter           : Pixel   = unit.pixelCenter
  val attackStarting        : Boolean = unit.attackStarting
  val hitPoints             : Int     = unit.hitPoints
  val shieldPoints          : Int     = unit.shieldPoints
  val defensiveMatrixPoints : Int     = unit.defensiveMatrixPoints
  val couldMoveThisFrame    : Boolean = unit.canMove
  val couldAttackThisFrame  : Boolean = unit.readyForAttackOrder
  val cooldown              : Int     = unit.cooldownLeft
  val tryingToAttack: Boolean = {
    if (unit.command.isEmpty) {
      false
    }
    else {
      val command = unit.command.get
      command.getUnitCommandType == UnitCommandType.Attack_Unit
    }
  }
  var tryingToMove: Boolean = {
    if (unit.command.isEmpty) {
      false
    }
    else {
      val command = unit.command.get
      command.getUnitCommandType == UnitCommandType.Move && unit.pixelDistanceFast(new Pixel(command.getTargetPosition)) > 128.0
    }
  }
  
  def age: Int = With.frame - frame
}
