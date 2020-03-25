package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

final class PathfindProfile(
   var start               : Tile,
   var end                 : Option[Tile]    = None,
   var endDistanceMaximum  : Float           = 0,
   var threatMaximum       : Option[Int]     = None,
   var lengthMinimum       : Option[Float]   = None,
   var lengthMaximum       : Option[Float]   = None, // It's hazardous to set a maximum with a specific destination when ground distance is off
   var canCrossUnwalkable  : Boolean         = false,
   var allowGroundDist     : Boolean         = false,
   var costOccupancy       : Float           = 0f,
   var costThreat          : Float           = 0f,
   var costRepulsion       : Float           = 0f,
   var costEnemyVision     : Float           = 0f,
   var repulsors: IndexedSeq[PathfindRepulsor] = IndexedSeq.empty,
   var unit: Option[FriendlyUnitInfo] = None) {

  def find: TilePath = With.paths.aStar(this)

  // Lil' hack -- track max repulsion statefully
  var maxRepulsion: Double = 0
  def updateRepulsion(): Unit = {
    maxRepulsion = repulsors.view.map(_.magnitude).sum
  }

}