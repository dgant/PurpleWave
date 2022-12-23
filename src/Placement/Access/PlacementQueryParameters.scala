package Placement.Access

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.{Important, PlaceLabel, Unimportant}
import ProxyBwapi.UnitClasses.UnitClass

class PlacementQueryParameters {
  var width     : Option[Int]       = None
  var height    : Option[Int]       = None
  var building  : Option[UnitClass] = None
  var labelYes  : Seq[PlaceLabel]   = Seq.empty
  var labelNo   : Seq[PlaceLabel]   = Seq.empty
  var zone      : Seq[Zone]         = Seq.empty
  var base      : Seq[Base]         = Seq.empty
  var tile      : Seq[Tile]         = Seq.empty

  private def repr[T](name: String, values: Iterable[T]): String = if (values.isEmpty) "" else f"$name(${values.mkString(" ")})"
  override def toString: String = f"PQP: ${repr("Width", width)} ${repr("Height", height)} ${repr("Bldg", building)} ${repr("Yes", labelYes)} ${repr("No", labelNo)} ${repr("Zone", zone)} ${repr("Base", base)} ${repr("Tile", tile)}".replaceAll("  ", " ")

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

  protected def scoreImportance(foundation: Foundation): Double = {
         if (foundation.point.requirement.labels.contains(Important))   1.2
    else if (foundation.point.requirement.labels.contains(Unimportant)) 1.0
    else                                                                1.1
  }

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
    val foundationBuildings = foundation.point.requirement.buildings
    // Request building     -> Has right building : Accept & prefer
    // Request building     -> Has wrong building : Reject hard
    // Request building     -> Lacks building     : Reject
    // Request no building  -> Has building       : Accept
    // Request no building  -> Lacks building     : Accept & prefer
          if (building.exists(foundationBuildings.contains))        2.0
    else  if (building.isDefined && foundationBuildings.nonEmpty) - 1.0
    else  if (building.isDefined)                                   0.0
    else  if (foundationBuildings.nonEmpty)                         1.0
    else                                                            2.0
  }

  protected def scoreLabelYes(foundation: Foundation): Double = {
    if (labelYes.isEmpty) return 1.0
    if (labelYes.forall(foundation.point.requirement.labels.contains)) return 1.0
    0.25 * labelYes.count(foundation.point.requirement.labels.contains) / labelYes.size
  }

  protected def scoreLabelNo(foundation: Foundation): Double = {
    if (labelNo.isEmpty) return 1.0
    if ( ! labelNo.exists(foundation.point.requirement.labels.contains)) return 1.0
    0.25 * labelNo.count( ! foundation.point.requirement.labels.contains(_)) / labelNo.size
  }

  protected def scoreZone(foundation: Foundation): Double = {
    if (zone.isEmpty) return 1.0
    if (zone.contains(foundation.tile.zone)) return 1.0
    0.25 * Maff.clamp(1 - Maff.nanToOne(zone.view.map(_.heart.groundTiles(foundation.tile)).min) / 256.0, 0.0, 1.0)
  }

  protected def scoreBase(foundation: Foundation): Double = {
    if (base.isEmpty) return 1.0
    if (base.exists(foundation.tile.base.contains)) return 1.0
    0.25 * Maff.clamp(1 - Maff.nanToOne(base.view.map(_.heart.groundTiles(foundation.tile)).min) / 256.0, 0.0, 1.0)
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

  def score(foundation: Foundation): Double = (
      1   * scoreImportance(foundation)
    + 1   * scoreWidth(foundation)
    + 1   * scoreHeight(foundation)
    + 1   * scoreLabelYes(foundation)
    + 1   * scoreLabelNo(foundation)
    + 5   * scoreZone(foundation)
    + 9   * scoreBase(foundation)
    + 0.5 * scoreBuilding(foundation)
    + 1   * scoreTile(foundation)
  )

  def accept(foundation: Foundation): Boolean = (
    acceptWidth(foundation)
    && acceptHeight(foundation)
    && acceptLabelYes(foundation)
    && acceptLabelNo(foundation)
    && acceptZone(foundation)
    && acceptBase(foundation)
    && acceptBuilding(foundation)
    && acceptTile(foundation))

  def audit(foundation: Foundation): (Foundation, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double) = (
    foundation,
    score(foundation),
    scoreImportance(foundation),
    scoreWidth(foundation),
    scoreHeight(foundation),
    scoreLabelYes(foundation),
    scoreLabelNo(foundation),
    scoreZone(foundation),
    scoreBase(foundation),
    scoreBuilding(foundation),
    scoreTile(foundation))
}
