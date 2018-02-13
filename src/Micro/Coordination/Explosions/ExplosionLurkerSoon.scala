package Micro.Coordination.Explosions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.{Force, ForceMath}
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionLurkerSoon(lurker: UnitInfo, target: UnitInfo) extends Explosion {
  
  val forceBullet     : Force = ForceMath.fromPixels(lurker.pixelCenter, target.pixelCenter)
  val forceMobility   : Force = target.mobilityForce
  val forceProjected  : Force = forceBullet.normalize(forceMobility * forceBullet.normalize)
  val forceMovement   : Force = (forceMobility - forceProjected).normalize(32)
  val targetPosition = target.pixelCenter
  
  override def affects(unit: UnitInfo): Boolean = unit == target && ! unit.flying
  override def framesRemaining: Double = lurker.cooldownMaxAgainst(target)
  
  override def draw(): Unit = DrawMap.arrow(target.pixelCenter, target.pixelCenter.add(forceMovement.toPoint), color)
  
  override def pixelsOfEntanglement(unit: UnitInfo): Double = (
    target.unitClass.radialHypotenuse
    + lurker.pixelRangeAgainst(target)
    - target.pixelDistanceEdge(lurker)
  )
  
  override def directionTo(unit: UnitInfo): Force = forceMovement
}
