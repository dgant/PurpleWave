package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitLinearGroundPush(val priority: TrafficPriority, val pusher: FriendlyUnitInfo, val destination: Pixel) extends LinearPush {
  override protected def source: Pixel = pusher.pixel
  override protected val sourceWidth: Double = pusher.unitClass.dimensionMax
  override def force(recipient: FriendlyUnitInfo): Option[Force] =
    if (recipient == pusher || recipient.flying || recipient.agent.priority >= pusher.agent.priority)
      None
    else
      super.force(recipient)
}
