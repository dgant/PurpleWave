package Placement.Access

import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Templating.PointGas
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.TileFilters.TileFilter

import scala.collection.mutable

class PlacementQuery extends TileFilter{
  var requirements  = new PlacementQueryOptions
  var preferences   = new PlacementQueryOptions

  /**
    * Recommended default arguments for a building type
    */
  def this(building: UnitClass) {
    this()
    requirements.width    = Some(building.tileWidthPlusAddon)
    requirements.height   = Some(building.tileHeight)
    requirements.building = Some(building).filter(b => b.isTownHall || b.isGas)
    preferences.width     = requirements.width
    preferences.height    = requirements.height
    preferences.building  = Some(building)
    preferences.zone      = With.geography.ourZones
    preferences.base      = With.geography.ourBases
    if (Seq(Terran.Barracks, Terran.Factory, Protoss.Gateway, Protoss.RoboticsFacility).contains(building)) {
      preferences.label = preferences.label :+ PlaceLabels.GroundProduction
    }
    if (Seq(
      Terran.EngineeringBay, Terran.Academy, Terran.Armory, Terran.Starport, Terran.ScienceFacility,
      Protoss.Forge, Protoss.CyberneticsCore, Protoss.Observatory, Protoss.RoboticsSupportBay, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.FleetBeacon, Protoss.ArbiterTribunal,
      Zerg.SpawningPool, Zerg.EvolutionChamber, Zerg.HydraliskDen, Zerg.Spire, Zerg.QueensNest, Zerg.DefilerMound, Zerg.UltraliskCavern).contains(building)) {
      preferences.label = preferences.label :+ PlaceLabels.Tech
    }
    if (Seq(Terran.MissileTurret, Terran.Bunker, Protoss.PhotonCannon, Protoss.ShieldBattery, Zerg.CreepColony).contains(building)) {
      preferences.label = preferences.label :+ PlaceLabels.Defensive
    }
    if (Seq(Terran.SupplyDepot, Protoss.Pylon).contains(building)) {
      preferences.label = preferences.label :+ PlaceLabels.Supply
    }
    if (Protoss.Pylon == building) {
      preferences.label = preferences.label :+ PlaceLabels.PriorityPower
    }
  }

  def apply(tile: Tile): Boolean = {
    if ( ! tile.valid) return false
    accept(Foundation(tile, With.placement.at(tile)))
  }

  def accept(foundation: Foundation): Boolean = {
    requirements.accept(foundation)
  }

  def score(foundation: Foundation): Double = {
    preferences.score(foundation)
  }

  def tiles: Traversable[Tile] = foundations.view.map(_.tile)

  def foundations: Traversable[Foundation] = {
    // As a performance optimization, start filtering from the smallest matching collection
    lazy val foundationSources = Seq(
      requirements.label.flatMap(With.placement.get(_).view),
      requirements.zone.flatMap(With.placement.get(_).view),
      requirements.base.flatMap(With.placement.get(_).view),
      requirements.building.map(With.placement.get(_).view).getOrElse(Seq.empty),
      requirements.width.flatMap(w => requirements.height.map(h => With.placement.get(w, h).view)).getOrElse(Seq.empty),
      With.placement.foundations.view)
    lazy val foundationSourceSmallest = foundationSources.filter(_.nonEmpty).minBy(_.size)

    val output = new mutable.PriorityQueue[Foundation]()(Ordering.by(preferences.score))
    if (requirements.building.exists(_.isGas)) {
      // We don't preplace gas foundations
      output ++= gasFoundations.filter(accept)
    } else {
      output ++= foundationSourceSmallest.filter(accept)
    }
    output
  }

  def gasFoundations: Traversable[Foundation] = {
    val bases = (if (requirements.base.isEmpty && requirements.zone.isEmpty) {
      With.geography.ourBases.view
    } else if (requirements.zone.isEmpty) {
      requirements.zone.view.flatMap(_.bases)
    } else {
      requirements.zone.view.flatMap(_.bases).filter(requirements.base.contains)
    })
    val gasses = bases.flatMap(_.gas).filter(_.isNeutral)
    val output = new mutable.PriorityQueue[Foundation]()(Ordering.by(preferences.score))
    output ++= gasses.map(_.tileTopLeft).map(Foundation(_, PointGas))
    output
  }

  def auditRequirements: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    With.placement.foundations.map(requirements.audit).sortBy(_._2)
  }

  def auditPreferences: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    With.placement.foundations.map(preferences.audit).sortBy(_._2)
  }

  def auditGasRequirements: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    gasFoundations.map(requirements.audit).toVector.sortBy(_._2)
  }

  def auditGasPreferences: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    gasFoundations.map(preferences.audit).toVector.sortBy(_._2)
  }
}
