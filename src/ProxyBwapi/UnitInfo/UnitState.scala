package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.Pixel

class UnitState(unit: UnitInfo) {
  val frame                 : Int     = With.frame
  
  val pixelCenter           : Pixel   = unit.pixelCenter
  var tryingToMove          : Boolean = unit.command.exists(command => { val p = command.getTargetPosition; p != null && unit.pixelDistanceFast(new Pixel(p)) > 16.0 })
  val hitPoints             : Int     = unit.hitPoints
  val shieldPoints          : Int     = unit.shieldPoints
  val defensiveMatrixPoints : Int     = unit.defensiveMatrixPoints
  
  def age: Int = With.frame - frame
}
