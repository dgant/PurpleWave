package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Information.Grids.Floody.AbstractGridFloody
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

final class PathfindProfile(
   var start                    : Tile,
   var end                      : Option[Tile]    = None,
   var endDistanceMaximum       : Float           = 0,
   var threatMaximum            : Option[Int]     = None, // Default: Ignore threat
   var lengthMinimum            : Option[Float]   = None,
   var lengthMaximum            : Option[Float]   = None, // It's hazardous to set a maximum with a specific destination when ground distance is off
   var canCrossUnwalkable       : Option[Boolean] = None, // Default: Reasonable value for unit, otherwise false
   var canEndUnwalkable         : Option[Boolean] = None, // Default: Reasonable value for unit, otherwise false
   var employGroundDist         : Boolean         = false,
   var costOccupancy            : Float           = 0f,
   var costThreat               : Float           = 0f,
   var costRepulsion            : Float           = 0f,
   var costEnemyVision          : Float           = 0f,
   var alsoUnwalkable           : Set[Tile]       = Set.empty,
   var repulsors: IndexedSeq[PathfindRepulsor] = IndexedSeq.empty,
   var unit: Option[FriendlyUnitInfo] = None) {

  lazy val crossUnwalkable: Boolean = {
    warnIfUnfinalized()
    canCrossUnwalkable.getOrElse(unit.exists(_.airborne))
  }
  lazy val endUnwalkable: Boolean = {
    warnIfUnfinalized()
    canEndUnwalkable.getOrElse(crossUnwalkable)
  }
  lazy val vulnerableByAir: Boolean = {
    warnIfUnfinalized()
    unit.map(u => u.flying || u.transport.exists(_.flying)).orElse(canCrossUnwalkable).getOrElse(true)
  }
  lazy val vulnerableByGround: Boolean = {
    warnIfUnfinalized()
    unit.map( ! _.flying).orElse(canCrossUnwalkable).getOrElse(true)
  }  
  lazy val threatGrid: AbstractGridFloody = {
    if (vulnerableByAir && vulnerableByGround)
      With.grids.enemyRangeAirGround
    else if (vulnerableByAir)
      With.grids.enemyRangeAir
    else if (vulnerableByGround)
      With.grids.enemyRangeGround
    else
      With.grids.enemyRangeAirGround
  }

  private def warnIfUnfinalized(): Unit = { if ( ! finalized) With.logger.warn("Attempted to use unfinalized PathfindProfile") }
  private var finalized: Boolean = false
  def find: TilePath = {
    finalized = true
    end = end.map(e => if (endUnwalkable) e else e.nearestWalkableTile)
    With.paths.aStar(this)
  }

  // Lil' hack -- track max repulsion statefully
  var maxRepulsion: Double = 0
  def updateRepulsion(): Unit = {
    maxRepulsion = repulsors.view.map(_.magnitude).sum
  }

}