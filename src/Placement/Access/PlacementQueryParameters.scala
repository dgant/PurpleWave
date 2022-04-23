package Placement.Access

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.PlaceLabel
import ProxyBwapi.UnitClasses.UnitClass

class PlacementQueryParameters {
  var width     : Option[Int]       = None
  var height    : Option[Int]       = None
  var building  : Option[UnitClass] = None
  var labelYes     : Seq[PlaceLabel]   = Seq.empty
  var labelNo  : Seq[PlaceLabel]   = Seq.empty
  var zone      : Seq[Zone]         = Seq.empty
  var base      : Seq[Base]         = Seq.empty
  var tile      : Seq[Tile]         = Seq.empty

  // Required for Produce to match requests against existing production
  override def equals(other: Any): Boolean = {
    if ( ! other.isInstanceOf[PlacementQueryParameters]) return false
    val otherParameters = other.asInstanceOf[PlacementQueryParameters]
    if (width     != otherParameters.width)     return false
    if (height    != otherParameters.height)    return false
    if (building  != otherParameters.building)  return false
    if ( ! labelYes.forall(otherParameters.labelYes.contains))  return false
    if ( ! otherParameters.labelYes.forall(labelYes.contains))  return false
    if ( ! zone.forall(otherParameters.zone.contains))    return false
    if ( ! otherParameters.zone.forall(zone.contains))    return false
    if ( ! base.forall(otherParameters.base.contains))    return false
    if ( ! otherParameters.base.forall(base.contains))    return false
    if ( ! tile.forall(otherParameters.tile.contains))    return false
    if ( ! otherParameters.tile.forall(tile.contains))    return false
    true
  }

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

  protected def scoreLabelYes(foundation: Foundation): Double = {
    if (labelYes.isEmpty) return 1.0
    if (labelYes.forall(foundation.point.requirement.labels.contains)) return 1.0
    labelYes.count(foundation.point.requirement.labels.contains) / (2.0 * labelYes.size)
  }

  protected def scoreLabelNo(foundation: Foundation): Double = {
    if (labelNo.isEmpty) return 1.0
    if ( ! labelNo.exists(foundation.point.requirement.labels.contains)) return 1.0
    labelNo.count( ! foundation.point.requirement.labels.contains(_)) / (2.0 * labelNo.size)
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

  protected def acceptWidth     (foundation: Foundation): Boolean = scoreWidth(foundation)    > acceptOver
  protected def acceptHeight    (foundation: Foundation): Boolean = scoreHeight(foundation)   > acceptOver
  protected def acceptBuilding  (foundation: Foundation): Boolean = scoreBuilding(foundation) > acceptOver
  protected def acceptLabelYes  (foundation: Foundation): Boolean = scoreLabelYes(foundation) > acceptOver
  protected def acceptLabelNo   (foundation: Foundation): Boolean = scoreLabelNo(foundation)  > acceptOver
  protected def acceptZone      (foundation: Foundation): Boolean = scoreZone(foundation)     > acceptOver
  protected def acceptBase      (foundation: Foundation): Boolean = scoreBase(foundation)     > acceptOver
  protected def acceptTile      (foundation: Foundation): Boolean = scoreTile(foundation)     > acceptOver

  def score(foundation: Foundation): Double = Seq(
    1 * scoreWidth(foundation),
    1 * scoreHeight(foundation),
    1 * scoreLabelYes(foundation),
    1 * scoreLabelNo(foundation),
    5 * scoreZone(foundation),
    9 * scoreBase(foundation),
    1 * scoreBuilding(foundation),
    1 * scoreTile(foundation)).sum

  def accept(foundation: Foundation): Boolean = (
    acceptWidth(foundation)
    && acceptHeight(foundation)
    && acceptLabelYes(foundation)
    && acceptLabelNo(foundation)
    && acceptZone(foundation)
    && acceptBase(foundation)
    && acceptBuilding(foundation)
    && acceptTile(foundation))

  def audit(foundation: Foundation): (Foundation, Double, Double, Double, Double, Double, Double, Double, Double, Double) = (
    foundation,
    score(foundation),
    scoreWidth(foundation),
    scoreHeight(foundation),
    scoreLabelYes(foundation),
    scoreLabelNo(foundation),
    scoreZone(foundation),
    scoreBase(foundation),
    scoreBuilding(foundation),
    scoreTile(foundation))
}
