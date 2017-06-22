package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.Pixel
import bwapi.UnitCommandType

class UnitState(unit: UnitInfo) {
  val frame                 : Int     = With.frame
  
  val pixelCenter           : Pixel   = unit.pixelCenter
  val hitPoints             : Int     = unit.hitPoints
  val shieldPoints          : Int     = unit.shieldPoints
  val defensiveMatrixPoints : Int     = unit.defensiveMatrixPoints
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
