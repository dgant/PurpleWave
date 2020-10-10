package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class UnitLinearPush(priority: Int, pusher: FriendlyUnitInfo, destination: Pixel) extends LinearPush {
  override protected def source: Pixel = pusher.pixelCenter
  override protected def range: Double = Math.min(source.pixelDistance(destination), 96)
  override protected val sourceWidth: Double = pusher.unitClass.dimensionMax

  override def force(recipient: FriendlyUnitInfo): Option[Force] = if (recipient == pusher) None else super.force(recipient)
}
