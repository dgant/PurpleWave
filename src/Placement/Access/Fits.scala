package Placement.Access

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Templating._
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Fits {
  protected lazy val points: Array[TemplatePoint] = Array.fill(With.mapTileArea)(PointAnything)
  private val byDimensions  = new mutable.HashMap[(Int, Int), ArrayBuffer[Foundation]]
  private val byBuilding    = new mutable.HashMap[UnitClass,  ArrayBuffer[Foundation]]
  private val byZone        = new mutable.HashMap[Zone,       ArrayBuffer[Foundation]]
  private val byLabel       = new mutable.HashMap[PlaceLabel, ArrayBuffer[Foundation]]
  val allFits               = new ArrayBuffer[Fit]

  def get(width: Int, height: Int)  : Seq[Foundation] = byDimensions.getOrElse((width, height), Seq.empty)
  def get(building: UnitClass)      : Seq[Foundation] = byBuilding.getOrElse(building, Seq.empty)
  def get(zone: Zone)               : Seq[Foundation] = byZone.getOrElse(zone, Seq.empty)
  def get(label: PlaceLabel)        : Seq[Foundation] = byLabel.getOrElse(label, Seq.empty)

  def requirementAt(tile: Tile): TemplatePoint = if (tile.valid) points(tile.i) else PointNothing

  def addFits(fits: Traversable[Fit]): Unit = {
    fits.foreach(addFit)
  }
  def addFit(fit: Fit): Unit = {
    allFits += fit
  }

  def place(fit: Fit): Unit = {
    fit.template.points.view.map(p => Foundation(fit.origin.add(p.point), p)).foreach(place)
  }
  def place(foundation: Foundation): Unit = {
    val point = foundation.point
    val requirement = point.requirement
    (0 until requirement.width).flatMap(dx =>
        (0 until requirement.height).map(foundation.tile.add(dx, _)))
          .filter(_.valid)
          .map(_.i)
          .foreach(points(_) = point)
    if ( ! requirement.buildableBefore) return
    if (requirement.buildableAfter) return
    byZone.put(foundation.tile.zone, byZone.getOrElse(foundation.tile.zone, new ArrayBuffer)).foreach(_ += foundation)
    requirement.buildings.foreach(b => byBuilding.put(b, byBuilding.getOrElse(b, new ArrayBuffer)).foreach(_ += foundation))
    if (requirement.buildings.isEmpty) {
      byDimensions.put(requirement.dimensions, byDimensions.getOrElse(requirement.dimensions, new ArrayBuffer)).foreach(_ += foundation)
    }
  }
}
