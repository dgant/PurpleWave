package Micro.Coordination.Pathing

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.TilePath
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MicroPath(unit: FriendlyUnitInfo) {
  val from: Pixel = unit.pixelCenter
  var to: Option[Pixel] = None
  var desire: Option[DesireProfile] = None
  var pathfindProfile: Option[PathfindProfile] = None
  var force: Option[Force] = None
  var tilePath: Option[TilePath] = None
}
