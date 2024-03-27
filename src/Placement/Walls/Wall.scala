package Placement.Walls

import Mathematics.Maff
import Mathematics.Points.{Point, Tile}
import Placement.Access.PlaceLabels
import Placement.Generation.Fit
import Placement.Templating.{RequireWalkable, Template, TemplatePointRequirement}
import Placement.Walls.WallProblems.WallProblem
import Placement.Walls.WallSpans.TerrainTerrain
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Wall {
  var constraint    : WallConstraint                  = WallConstraint(0, Protoss.Dragoon, TerrainTerrain)
  val buildings     : ArrayBuffer[(Tile, UnitClass)]  = new ArrayBuffer[(Tile, UnitClass)]
  var problems      : mutable.HashSet[WallProblem]    = new mutable.HashSet[WallProblem]
  var gap           : Option[Tile]                    = None
  var hallway       : Seq[Tile]                       = Seq.empty
  var score         : Double                          = _

  lazy val defensePoint: Tile = gap.getOrElse({
    val buildingAreas = buildings.map(b => b._2.tileAreaPlusAddon.add(b._1))
    val centroid      = Maff.exemplar(buildingAreas.flatMap(_.tiles).map(_.center))
    val searchArea    = buildingAreas.reduce(_.add(_)).expand(1, 1)
    searchArea.tiles
      .filter(_.walkable)
      .filterNot(t => buildingAreas.exists(_.contains(t)))
      .minBy(_.tileDistanceFast(centroid.zone.heart))
  })

  def this(other: Wall) {
    this()
    constraint  = other.constraint
    buildings ++= other.buildings
    problems  ++= other.problems
    gap         = other.gap
    hallway     = other.hallway
    score       = other.score
  }

  def toFit: Fit = {
    if (buildings.isEmpty) {
      return Fit(Tile(0, 0), new Template())
    }

    val origin  = Tile(buildings.view.map(_._1.x).min, buildings.view.map(_._1.x).max)
    val fit     = Fit(origin, new Template)

    buildings.foreach(b => fit.template.add(
      Point(b._1.x - origin.x, b._1.y - origin.y),
      new TemplatePointRequirement(b._2).withLabels(PlaceLabels.Defensive, PlaceLabels.DefendEntrance, PlaceLabels.DefendGround, PlaceLabels.Wall)))

    hallway.foreach(tile => fit.template.add(
      Point(tile.x - origin.x, tile.y - origin.y),
      new RequireWalkable))

    fit
  }

  override def toString: String = f"Wall: $constraint -> Buildings: $buildings, Gap: $gap, Hallway: $hallway"
}
