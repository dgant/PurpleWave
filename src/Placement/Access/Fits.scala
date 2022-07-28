package Placement.Access

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.PlaceLabel
import Placement.Generation.Fit
import Placement.Templating._
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Fits {
  protected lazy val points: Array[TemplatePoint]       = Array.fill(With.mapTileArea)(new PointAnything)
  protected lazy val byTile: Array[Option[Foundation]]  = Array.fill(With.mapTileArea)(None)
  private val _fits         = new ArrayBuffer[Fit] // We only keep these for debugging
  private val _foundations  = new ArrayBuffer[Foundation]
  private val byDimensions  = new mutable.HashMap[(Int, Int), ArrayBuffer[Foundation]]
  private val byZone        = new mutable.HashMap[Zone,       ArrayBuffer[Foundation]]
  private val byBase        = new mutable.HashMap[Base,       ArrayBuffer[Foundation]]
  private val byBuilding    = new mutable.HashMap[UnitClass,  ArrayBuffer[Foundation]]
  private val byLabelYes    = new mutable.HashMap[PlaceLabel, ArrayBuffer[Foundation]]

  def at(tile: Tile)                : TemplatePoint       = if (tile.valid) points(tile.i) else new PointNothing
  def get(tile: Tile)               : Option[Foundation]  = if (tile.valid) byTile(tile.i) else None
  def get(width: Int, height: Int)  : Seq[Foundation]     = byDimensions.getOrElse((width, height), Seq.empty)
  def get(zone: Zone)               : Seq[Foundation]     = byZone.getOrElse(zone, Seq.empty)
  def get(base: Base)               : Seq[Foundation]     = byBase.getOrElse(base, Seq.empty)
  def get(building: UnitClass)      : Seq[Foundation]     = byBuilding.getOrElse(building, Seq.empty)
  def get(label: PlaceLabel)        : Seq[Foundation]     = byLabelYes.getOrElse(label, Seq.empty)
  def foundations                   : Seq[Foundation]     = _foundations
  def fits                          : Seq[Fit]            = _fits

  def index(fit: Fit): Unit = {
    _fits += fit
    fit.template.points.view.map(p => Foundation(fit.origin.add(p.point), p, fit.order)).foreach(index)
  }
  protected def index(foundation: Foundation): Unit = {
    if ( ! foundation.tile.valid) return
    val point = foundation.point
    val requirement = point.requirement
    (0 until requirement.width).flatMap(dx =>
        (0 until requirement.height).map(foundation.tile.add(dx, _)))
          .filter(_.valid)
          .map(_.i)
          .foreach(points(_) = point)
    byTile(foundation.tile.i) = Some(foundation)
    if ( ! requirement.buildableBefore) return
    if (requirement.buildableAfter) return
    _foundations += foundation
    put(byDimensions, foundation.point.requirement.dimensions,  foundation)
    put(byZone,       foundation.tile.zone,                     foundation)
    foundation.tile.base  .foreach(base     => put(byBase,      base,     foundation))
    requirement.buildings .foreach(building => put(byBuilding,  building, foundation))
    requirement.labels    .foreach(label    => put(byLabelYes,  label,    foundation))
  }

  private def put[T, U](map: mutable.Map[T, ArrayBuffer[U]], key: T, value: U): Unit = {
    map.put(key, map.getOrElse(key, new ArrayBuffer)).foreach(_ += value)
  }

  protected def sort(): Unit = {
    Maff.sortStablyInPlaceBy(_fits)(_.order)
    _fits.sortBy(_.order)
    Maff.sortStablyInPlaceBy(_foundations)(_.order)
    byDimensions  .foreach(b => Maff.sortStablyInPlaceBy(b._2)(_.order))
    byZone        .foreach(b => Maff.sortStablyInPlaceBy(b._2)(_.order))
    byBase        .foreach(b => Maff.sortStablyInPlaceBy(b._2)(_.order))
    byBuilding    .foreach(b => Maff.sortStablyInPlaceBy(b._2)(_.order))
    byLabelYes    .foreach(b => Maff.sortStablyInPlaceBy(b._2)(_.order))
  }
}
