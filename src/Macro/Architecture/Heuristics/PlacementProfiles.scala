package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint

object PlacementProfiles {
  
  def default(blueprint: Blueprint): PlacementProfile = {
    if (blueprint.requireTownHallTile.get)
      townHall
    else if (blueprint.requireGasTile.get)
      gas
    else if (blueprint.powers.get)
      pylon
    else if (blueprint.building.exists(_.trainsGroundUnits))
      factory
    else if (blueprint.building.exists(_.rawCanAttack))
      cannon
    else
      tech
  }
  
  val basic = new PlacementProfile(
    "Basic",
    preferZone                  = 1.0,
    preferNatural               = 0.0,
    preferGas                   = 0.0,
    preferSpace                 = 0.2,
    preferPowering              = 0.3,
    preferDistanceFromEnemy     = 0.6,
    preferCoveringWorkers       = 0.0,
    preferSurfaceArea           = 1.0,
    avoidDistanceFromBase       = 0.3,
    avoidDistanceFromEnemy      = 0.0,
    avoidDistanceFromIdealRange = 0.1,
    avoidSurfaceArea            = 0.0
  )
  
  val pylon = new PlacementProfile("Pylon", basic) {
    preferPowering = 0.25
  }
  
  val factory = new PlacementProfile("Factory", basic) {
    preferDistanceFromEnemy = 0.0
  }
  
  val tech = new PlacementProfile("Tech", basic) {
    preferDistanceFromEnemy     = 3.0
    avoidDistanceFromBase       = 1.0
  }
  
  val gas = new PlacementProfile("Gas",
    preferZone                  = 100.0,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 1.0
  )
  
  val townHall = new PlacementProfile("Town Hall",
    preferNatural               = 6.0,
    preferGas                   = 1.0,
    preferDistanceFromEnemy     = 1.5,
    avoidDistanceFromBase       = 2.0
  )
  
  //////////////////////////
  // Specialty placements //
  //////////////////////////
  
  val naturalCannonPylon = new PlacementProfile(
    "Pylon for natural Cannons",
    preferZone                  = 100.0,
    preferNatural               = 30.0,
    preferPowering              = 0.5,
    preferDistanceFromEnemy     = 0.5,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 1.5,
    avoidSurfaceArea            = 0.25,
    avoidDistanceFromIdealRange = 5.0)
  
  val naturalCannon = new PlacementProfile(
    "Natural cannons",
    preferZone                  = 100.0,
    preferNatural               = 30.0,
    preferDistanceFromEnemy     = 0.5,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 1.5,
    avoidSurfaceArea            = 0.5,
    avoidDistanceFromIdealRange = 5.0)
  
  val cannon = new PlacementProfile("Defense", basic) {
    preferNatural               = 5.0
    preferCoveringWorkers       = 5.0
    avoidDistanceFromBase       = 5.0
    avoidDistanceFromEnemy      = 5.0
  }
  
  val proxyBuilding = new PlacementProfile(
    "Proxy",
    avoidSurfaceArea            = 1.0)
  
  val proxyPylon = new PlacementProfile(
    "Proxy Pylon",
    preferZone                  = 5.0,
    preferPowering              = 1.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val hugTheNexus = new PlacementProfile(
    "Hugging Nexus",
    preferPowering        = 0.5,
    avoidDistanceFromBase = 1.0)
}
