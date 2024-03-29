package Placement.Walls

import Mathematics.Points.{Point, Tile}
import Placement.Access.PlaceLabels
import Placement.Generation.Fit
import Placement.Templating.{RequireWalkable, Template, TemplatePointRequirement}
import Placement.Walls.WallSpans.TerrainTerrain
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Wall {
  val buildings   : ArrayBuffer[(Tile, UnitClass)]  = new ArrayBuffer[(Tile, UnitClass)]
  var constraint  : WallConstraint                  = WallConstraint(0, Protoss.Dragoon, TerrainTerrain)
  var gap         : Option[Tile]                    = None
  var hallway     : Seq[Tile]                       = Seq.empty

  def this(other: Wall) {
    this()
    buildings ++= other.buildings
    constraint  = other.constraint
    gap         = other.gap
    hallway     = other.hallway
  }

  def toFit: Fit = {
    if (buildings.isEmpty) return Fit(Tile(0, 0), new Template())
    val origin = Tile(buildings.view.map(_._1.x).min, buildings.view.map(_._1.x).max)
    val fit = Fit(origin, new Template)
    buildings.foreach(b => fit.template.add(
      Point(b._1.x - origin.x, b._1.y - origin.y),
      new TemplatePointRequirement(b._2).withLabels(PlaceLabels.Defensive, PlaceLabels.DefendEntrance, PlaceLabels.DefendGround, PlaceLabels.Wall)))
    hallway.foreach(tile => fit.template.add(
      Point(tile.x - origin.x, tile.y - origin.y),
      new RequireWalkable))
    fit
  }
}
