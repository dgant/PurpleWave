package Placement.Access

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.PlaceLabel
import Placement.Generation.Fit
import Placement.Templating._
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Fits {
  protected lazy val points: Array[TemplatePoint] = Array.fill(With.mapTileArea)(PointAnything)
  private val _fits         = new ArrayBuffer[Fit] // We only keep these for debugging
  private val _foundations  = new ArrayBuffer[Foundation]
  private val byDimensions  = new mutable.HashMap[(Int, Int), ArrayBuffer[Foundation]]
  private val byZone        = new mutable.HashMap[Zone,       ArrayBuffer[Foundation]]
  private val byBase        = new mutable.HashMap[Base,       ArrayBuffer[Foundation]]
  private val byBuilding    = new mutable.HashMap[UnitClass,  ArrayBuffer[Foundation]]
  private val byLabel       = new mutable.HashMap[PlaceLabel, ArrayBuffer[Foundation]]

  def at(tile: Tile)                : TemplatePoint   = if (tile.valid) points(tile.i) else PointNothing
  def get(width: Int, height: Int)  : Seq[Foundation] = byDimensions.getOrElse((width, height), Seq.empty)
  def get(zone: Zone)               : Seq[Foundation] = byZone.getOrElse(zone, Seq.empty)
  def get(base: Base)               : Seq[Foundation] = byBase.getOrElse(base, Seq.empty)
  def get(building: UnitClass)      : Seq[Foundation] = byBuilding.getOrElse(building, Seq.empty)
  def get(label: PlaceLabel)        : Seq[Foundation] = byLabel.getOrElse(label, Seq.empty)
  def foundations                   : Seq[Foundation] = _foundations
  def fits                          : Seq[Fit]        = _fits

  def index(fit: Fit): Unit = {
    _fits += fit
    fit.template.points.view.map(p => Foundation(fit.origin.add(p.point), p, fit.order)).foreach(index)
  }
  protected def index(foundation: Foundation): Unit = {
    val point = foundation.point
    val requirement = point.requirement
    (0 until requirement.width).flatMap(dx =>
        (0 until requirement.height).map(foundation.tile.add(dx, _)))
          .filter(_.valid)
          .map(_.i)
          .foreach(points(_) = point)
    if ( ! requirement.buildableBefore) return
    if (requirement.buildableAfter) return
    _foundations += foundation
    put(byDimensions, foundation.point.requirement.dimensions, foundation)
    put(byZone, foundation.tile.zone, foundation)
    foundation.tile.base.foreach(base => put(byBase, base, foundation))
    requirement.buildings.foreach(b => put(byBuilding, b, foundation))
    requirement.labels.foreach(l => put(byLabel, l, foundation))

    if (requirement.buildings.isEmpty) {
      byDimensions.put(requirement.dimensions, byDimensions.getOrElse(requirement.dimensions, new ArrayBuffer)).foreach(_ += foundation)
    }
  }

  private def put[T, U](map: mutable.Map[T, ArrayBuffer[U]], key: T, value: U): Unit = {
    map.put(key, map.getOrElse(key, new ArrayBuffer)).foreach(_ += value)
  }

  private def sort(): Unit = {
    /*
    _fits.sortBy(_.order)
    _foundations.sortBy(_.order)
    byDimensions.foreach(_.)
    */
  }
}
