package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Push {
  val priority: Int
  def tiles: Seq[Tile]
  def force(recipient: FriendlyUnitInfo): Option[Force]
}
