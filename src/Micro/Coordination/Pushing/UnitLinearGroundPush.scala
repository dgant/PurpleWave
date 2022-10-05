package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

class UnitLinearGroundPush(val priority: TrafficPriority, val pusher: FriendlyUnitInfo, val destination: Pixel) extends LinearPush {
  override protected def source: Pixel = pusher.pixel
  override protected val sourceWidth: Double = pusher.unitClass.dimensionMax
  override def force(recipient: FriendlyUnitInfo): Option[Force] = ?(recipient == pusher || recipient.flying, None, super.force(recipient))
}
