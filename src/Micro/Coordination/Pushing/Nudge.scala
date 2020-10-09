package Micro.Coordination.Pushing

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class Nudge(pusher: FriendlyUnitInfo, destination: Pixel) extends UnitLinearPush(pusher, destination) {
  override val priority: Integer = PushPriority.Nudge
}
