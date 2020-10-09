package Micro.Coordination.Pushing

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class UnitLinearPush(pusher: FriendlyUnitInfo, destination: Pixel) extends LinearPush {
  override protected def source: Pixel = pusher.pixelCenter
  override protected def range: Double = Math.min(source.pixelDistance(destination), 96)
  override protected val sourceWidth: Double = pusher.unitClass.dimensionMax
}
