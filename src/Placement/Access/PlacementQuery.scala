package Placement.Access

import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Templating.PointGas
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

class PlacementQuery {
  var requirements  = new PlacementQueryParameters
  var preferences   = new PlacementQueryParameters
  // Required for Produce to match requests against existing production
  override def equals(other: Any): Boolean = {
    if ( ! other.isInstanceOf[PlacementQuery]) return false
    val otherQuery = other.asInstanceOf[PlacementQuery]
    requirements == otherQuery.requirements && preferences == otherQuery.preferences
  }

  /**
    * Recommended default arguments for a building type
    */
  def this(building: UnitClass) {
    this()
    resetDefaults(building)
  }

  def resetDefaults(building: UnitClass): Unit = {
    requirements.width    = Some(building.tileWidthPlusAddon)
    requirements.height   = Some(building.tileHeight)
    requirements.building = Some(building).filter(b => b.isTownHall || b.isGas)
    requirements.labelYes = if (building.isTownHall) Seq(PlaceLabels.TownHall)  else Seq.empty
    requirements.labelNo  = if (building.isTownHall) Seq.empty                  else Seq(PlaceLabels.TownHall)
    preferences.width     = requirements.width
    preferences.height    = requirements.height
    preferences.building  = Some(building)
    preferences.zone      = With.geography.ourZones
    preferences.base      = With.geography.ourBases
    if (Seq(Terran.Barracks, Terran.Factory, Protoss.Gateway, Protoss.RoboticsFacility).contains(building)) {
      preferences.labelYes = preferences.labelYes :+ PlaceLabels.GroundProduction
    }
    if (Seq(
      Terran.EngineeringBay, Terran.Academy, Terran.Armory, Terran.Starport, Terran.ScienceFacility,
      Protoss.Forge, Protoss.CyberneticsCore, Protoss.Observatory, Protoss.RoboticsSupportBay, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.FleetBeacon, Protoss.ArbiterTribunal,
      Zerg.SpawningPool, Zerg.EvolutionChamber, Zerg.HydraliskDen, Zerg.Spire, Zerg.QueensNest, Zerg.DefilerMound, Zerg.UltraliskCavern).contains(building)) {
      preferences.labelYes = preferences.labelYes :+ PlaceLabels.Tech
    }
    if (Seq(Terran.MissileTurret, Terran.Bunker, Protoss.PhotonCannon, Protoss.ShieldBattery, Zerg.CreepColony).contains(building)) {
      preferences.labelYes = preferences.labelYes :+ PlaceLabels.Defensive
    }
    if (Seq(Terran.SupplyDepot, Protoss.Pylon).contains(building)) {
      preferences.labelYes = preferences.labelYes :+ PlaceLabels.Supply
    }
    if (Protoss.Pylon == building) {
      preferences.labelYes = preferences.labelYes :+ PlaceLabels.PriorityPower
    }
  }

  def acceptExisting(tile: Tile): Boolean = {
    if ( ! tile.valid) return false
    if (requirements.zone.nonEmpty    && ! requirements.zone.contains(tile.zone)) return false
    if (requirements.base.nonEmpty    && ! requirements.base.exists(tile.base.contains)) return false
    if (requirements.tile.nonEmpty    && ! requirements.tile.contains(tile)) return false
    if ( ! requirements.labelYes.forall(With.placement.at(tile).requirement.labels.contains)) return false
    if (    requirements.labelNo.exists(With.placement.at(tile).requirement.labels.contains)) return false
    true
  }

  def accept(foundation: Foundation): Boolean = {
    requirements.accept(foundation)
  }

  def score(foundation: Foundation): Double = {
    preferences.score(foundation)
  }

  def tiles: Traversable[Tile] = foundations.view.map(_.tile)

  def foundations: Seq[Foundation] = {
    // As a performance optimization, start filtering from the smallest matching collection
    lazy val foundationSources = IndexedSeq(
      requirements.labelYes.flatMap(With.placement.get),
      requirements.zone.flatMap(With.placement.get),
      requirements.base.flatMap(With.placement.get),
      requirements.building.map(With.placement.get).getOrElse(IndexedSeq.empty),
      requirements.width.flatMap(w => requirements.height.map(h => With.placement.get(w, h))).getOrElse(IndexedSeq.empty),
      With.placement.foundations)
    lazy val foundationSourceSmallest = foundationSources.filter(_.nonEmpty).minBy(_.size)
    (if (requirements.building.exists(_.isGas)) gasFoundations else foundationSourceSmallest).filter(accept)sortBy(- score(_))
    // If this sorting is too slow we can use a PriorityQueue,
    // but would need to account for the existing ordering in the score
    // because PriorityQueue is not a stable sort
  }

  def gasFoundations: Seq[Foundation] = {
    val bases = (if (requirements.base.isEmpty && requirements.zone.isEmpty) {
      With.geography.ourBases.view
    } else if (requirements.zone.isEmpty) {
      requirements.zone.view.flatMap(_.bases)
    } else {
      requirements.zone.view.flatMap(_.bases).filter(requirements.base.contains)
    })
    bases.flatMap(_.gas).filter(_.isNeutral).map(_.tileTopLeft).map(Foundation(_, PointGas))
  }

  def auditRequirements: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    With.placement.foundations.map(requirements.audit).sortBy(-_._2)
  }

  def auditPreferences: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    foundations.map(preferences.audit).sortBy(-_._2)
  }

  def auditPreferencesUnfiltered: Seq[(Foundation, Double, Double, Double, Double, Double, Double, Double, Double, Double)] = {
    With.placement.foundations.map(preferences.audit).sortBy(-_._2)
  }
}
