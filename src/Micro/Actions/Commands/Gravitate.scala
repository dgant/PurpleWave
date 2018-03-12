package Micro.Actions.Commands

import Lifecycle.With
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gravitate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.forces.nonEmpty
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    val forces          = unit.agent.forces.values
    val origin          = unit.pixelCenter
    val forceTotal      = ForceMath.sum(forces)
    val framesAhead     = With.reaction.agencyAverage + 1
    val distance        = unit.unitClass.haltPixels + framesAhead * (unit.topSpeed + framesAhead * unit.unitClass.accelerationFrames / 2)
    val forceNormal     = forceTotal.normalize(distance)
    val forcePoint      = forceNormal.toPoint
    val destination     = origin.add(forcePoint)
    unit.agent.toTravel = Some(destination)
  }
}
