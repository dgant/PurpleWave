package Placement.Templating

import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object PlaceLabels {

  private val defensive = Seq(
    Terran.Bunker,
    Terran.MissileTurret,
    Protoss.Pylon,
    Protoss.ShieldBattery,
    Protoss.PhotonCannon,
    Zerg.CreepColony,
    Zerg.SunkenColony,
    Zerg.SporeColony)

  private val groundProduction = Seq(
    Terran.Barracks,
    Terran.Factory,
    Protoss.Gateway,
    Protoss.RoboticsFacility,
    Zerg.Hatchery,
    Zerg.Lair,
    Zerg.Hive)

  private val tech = Seq(
    Terran.EngineeringBay,
    Terran.Academy,
    Terran.Armory,
    Terran.Starport,
    Terran.ScienceFacility,
    Protoss.CyberneticsCore,
    Protoss.Forge,
    Protoss.Observatory,
    Protoss.RoboticsSupportBay,
    Protoss.CitadelOfAdun,
    Protoss.TemplarArchives,
    Protoss.Stargate,
    Protoss.FleetBeacon,
    Protoss.ArbiterTribunal,
    Zerg.SpawningPool,
    Zerg.EvolutionChamber,
    Zerg.HydraliskDen,
    Zerg.Spire,
    Zerg.QueensNest,
    Zerg.UltraliskCavern,
    Zerg.DefilerMound)

  private val supply = Seq(
    Terran.SupplyDepot,
    Protoss.Pylon)

  object DefendChoke extends PlaceLabel
  object DefendWorkers extends PlaceLabel
  object Tech extends PlaceLabel
  object PriorityPower extends PlaceLabel
}
