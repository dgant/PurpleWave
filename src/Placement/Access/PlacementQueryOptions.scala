package Placement.Access

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.PlaceLabel
import ProxyBwapi.UnitClasses.UnitClass

class PlacementQueryOptions {
  var width     : Option[Int]       = None
  var height    : Option[Int]       = None
  var building  : Option[UnitClass] = None
  var label     : Seq[PlaceLabel]   = Seq.empty
  var zone      : Seq[Zone]         = Seq.empty
  var base      : Seq[Base]         = Seq.empty
  var tile      : Seq[Tile]         = Seq.empty

  private val acceptOver: Double = 0.99

  protected def scoreWidth(foundation: Foundation): Double = {
    if (width.isEmpty) return 1.0
    if (foundation.point.requirement.width == width.get) return 1.25
    if (foundation.point.requirement.width >  width.get) return 1.0
    0.0
  }

  protected def scoreHeight(foundation: Foundation): Double = {
    if (height.isEmpty) return 1.0
    if (foundation.point.requirement.height == height.get) return 1.25
    if (foundation.point.requirement.height >  height.get) return 1.0
    0.0
  }

  protected def scoreBuilding(foundation: Foundation): Double = {
    if (building.exists(foundation.point.requirement.buildings.contains)) return 1.0
    if (building.isEmpty) return 1.0
    val widthMatches = foundation.point.requirement.width == building.get.tileWidth
    val heightMatches = foundation.point.requirement.height == building.get.tileHeight
    if (widthMatches && heightMatches) return 0.75
    0.0
  }

  protected def scoreLabel(foundation: Foundation): Double = {
    if (label.isEmpty) return 1.0
    if (label.forall(foundation.point.requirement.labels.contains)) return 1.0
    label.count(foundation.point.requirement.labels.contains) / (2.0 * label.size)
  }

  protected def scoreZone(foundation: Foundation): Double = {
    if (zone.isEmpty) return 1.0
    if (zone.contains(foundation.tile.zone)) return 1.0
    if (foundation.tile.metro.exists(_.zones.exists(zone.contains))) return 0.25
    0.0
  }

  protected def scoreBase(foundation: Foundation): Double = {
    if (base.isEmpty) return 1.0
    if (base.exists(foundation.tile.base.contains)) return 1.0
    if (base.exists(_.zone == foundation.tile.zone)) return 0.5
    if (foundation.tile.metro.exists(_.bases.exists(base.contains))) return 0.25
    0.0
  }

  protected def scoreTile(foundation: Foundation): Double = {
    if (tile.isEmpty) return 1.0
    if (tile.contains(foundation.tile)) return 1.0
    Maff.clamp(0.25 * Maff.nanToOne(With.mapTileWidth.toDouble / tile.map(_.groundTiles(foundation.tile)).min), 0.0, 0.25)
  }

  protected def acceptWidth(foundation: Foundation): Boolean = scoreWidth(foundation) > acceptOver
  protected def acceptHeight(foundation: Foundation): Boolean = scoreHeight(foundation) > acceptOver
  protected def acceptBuilding(foundation: Foundation): Boolean = scoreBuilding(foundation) > acceptOver
  protected def acceptLabel(foundation: Foundation): Boolean = scoreLabel(foundation) > acceptOver
  protected def acceptZone(foundation: Foundation): Boolean = scoreZone(foundation) > acceptOver
  protected def acceptBase(foundation: Foundation): Boolean = scoreBase(foundation) > acceptOver
  protected def acceptTile(foundation: Foundation): Boolean = scoreTile(foundation) > acceptOver

  def score(foundation: Foundation): Double = Seq(
    scoreWidth(foundation),
    scoreHeight(foundation),
    scoreLabel(foundation),
    scoreZone(foundation),
    scoreBase(foundation),
    scoreBuilding(foundation),
    scoreTile(foundation)).sum

  def accept(foundation: Foundation): Boolean = (
    acceptWidth(foundation)
    && acceptHeight(foundation)
    && acceptLabel(foundation)
    && acceptZone(foundation)
    && acceptBase(foundation)
    && acceptBuilding(foundation)
    && acceptTile(foundation))

  def audit(foundation: Foundation): (Foundation, Double, Double, Double, Double, Double, Double, Double, Double) = (
    foundation,
    score(foundation),
    scoreWidth(foundation),
    scoreHeight(foundation),
    scoreLabel(foundation),
    scoreZone(foundation),
    scoreBase(foundation),
    scoreBuilding(foundation),
    scoreTile(foundation))
}
