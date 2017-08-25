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
    else if (blueprint.building.exists(_.attacks))
      cannon
    else
      tech
  }
  
  val basic = new PlacementProfile(
    "Basic",
    preferZone                  = 1.0,
    preferNatural               = 0.0,
    preferResources             = 0.0,
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
    preferSurfaceArea           = 0.1
    avoidDistanceFromBase       = 1.0
  }
  
  val gas = new PlacementProfile("Gas",
    preferZone                  = 1000.0,
    avoidDistanceFromBase       = 1.0
  )
  
  val townHall = new PlacementProfile("Town Hall",
    preferZone                  = 1000.0,
    preferNatural               = 6.0,
    preferResources             = 0.5,
    preferDistanceFromEnemy     = 1.5,
    avoidDistanceFromBase       = 2.0
  )
  
  //////////////////////////
  // Specialty placements //
  //////////////////////////
  
  val cannonPylon = new PlacementProfile(
    "Pylon for Cannons",
    preferZone                  = 1000.0,
    preferNatural               = 10.0,
    preferPowering              = 1.0,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 0.5,
    avoidSurfaceArea            = 0.05,
    avoidDistanceFromIdealRange = 0.5)
  
  val cannon = new PlacementProfile(
    "Cannons",
    preferZone                  = 1000.0,
    preferNatural               = 10.0,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 1.0,
    avoidSurfaceArea            = 0.25,
    avoidDistanceFromEnemy      = 0.5,
    avoidDistanceFromIdealRange = 3.0)
  
  val proxyBuilding = new PlacementProfile(
    "Proxy",
    preferZone                  = 1000.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val proxyPylon = new PlacementProfile(
    "Proxy Pylon",
    preferZone                  = 1000.0,
    preferPowering              = 1.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val proxyCannon = new PlacementProfile(
    "Proxy cannon",
    preferZone                  = 1000.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val hugTownHall = new PlacementProfile(
    "Hug town hall",
    preferPowering              = 0.5,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 0.1)
  
  val hugWorkersWithPylon = new PlacementProfile(
    "Hug workers with Pylon",
    preferPowering              = 0.5,
    preferCoveringWorkers       = 1.0)
  
  val hugWorkersWithCannon = new PlacementProfile(
    "Hug workers with cannon",
    preferCoveringWorkers       = 1.0)
}
