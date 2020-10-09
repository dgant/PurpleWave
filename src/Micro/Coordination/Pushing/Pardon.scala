package Micro.Coordination.Pushing

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class Pardon(pusher: FriendlyUnitInfo, destination: Pixel) extends UnitLinearPush(pusher, destination) {
  override val priority: Integer = PushPriority.Pardon
}
