package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitLinearPush(val priority: Int, val pusher: FriendlyUnitInfo, val destination: Pixel) extends LinearPush {
  override protected def source: Pixel = pusher.pixelCenter
  override protected val sourceWidth: Double = pusher.unitClass.dimensionMax
  override def force(recipient: FriendlyUnitInfo): Option[Force] = if (recipient == pusher) None else super.force(recipient)
}
