package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import ProxyBwapi.Races.Terran

object PlacementProfiles {
  
  def default(blueprint: Blueprint): PlacementProfile = {
    if (blueprint.requireTownHallTile.get)
      townHall
    else if (blueprint.requireGasTile.get)
      gas
    else if (blueprint.powers.get)
      pylon
    else if (blueprint.building.exists(_.trainsGroundUnits))
      if (blueprint.building.contains(Terran.Barracks)) factoryNoSpace else factory
    else if (blueprint.building.exists(_.attacks))
      wallCannon
    else
      tech
  }
  
  val basic = new PlacementProfile(
    "Basic",
    preferZone                  = 1.0,
    preferNatural               = 0.0,
    preferResources             = 0.0,
    preferSpace                 = 0.05,
    preferPowering              = 0.1,
    preferDistanceFromEnemy     = 0.6,
    preferCoveringWorkers       = 0.0,
    preferSurfaceArea           = 0.5,
    avoidDistanceFromBase       = 0.3,
    avoidDistanceFromEnemy      = 0.0,
    avoidDistanceFromIdealRange = 0.05,
    avoidSurfaceArea            = 0.0
  )
  
  val pylon = new PlacementProfile("Pylon", basic)
  val factory = new PlacementProfile("Factory", basic)
  val factoryNoSpace = new PlacementProfile("Factory", basic) {
    preferSpace = 0.0
  }
  
  val tech = new PlacementProfile("Tech", basic) {
    preferSpace                 = 0.0
    preferDistanceFromEnemy     = 3.0
    preferSurfaceArea           = 0.1
    avoidDistanceFromBase       = 1.0
    avoidDistanceFromIdealRange = 0.0
  }
  
  val gas = new PlacementProfile("Gas",
    preferZone                  = 1000.0,
    avoidDistanceFromBase       = 1.0,
    preferDistanceFromEnemy     = 1.0
  )
  
  val townHall = new PlacementProfile("Town Hall",
    preferZone                  = 1000.0,
    preferNatural               = 1.0,
    preferResources             = 0.5,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 1.0
  )
  
  //////////////////////////
  // Specialty placements //
  //////////////////////////
  
  val backPylon = new PlacementProfile("Pylon for the back of your base", basic) {
    preferDistanceFromEnemy     = 1.0
    avoidDistanceFromIdealRange = 0.0
  }
  
  val wallPylon = new PlacementProfile(
    "Pylon for a wall",
    preferZone                  = 1000.0,
    preferNatural               = 10.0,
    preferPowering              = 1.0,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromEntrance   = 0.5,
    avoidSurfaceArea            = 0.05,
    avoidDistanceFromIdealRange = 0.5)
  
  val wallCannon = new PlacementProfile(
    "Cannons for a wall",
    preferZone                  = 1000.0,
    preferNatural               = 10.0,
    preferCoveringWorkers       = 0.2,
    avoidDistanceFromEntrance   = 1.0,
    avoidSurfaceArea            = 0.1,
    avoidDistanceFromEnemy      = 0.5,
    avoidDistanceFromIdealRange = 1.75)
  
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
    preferPowering              = 0.1,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 0.1)
  
  val hugWorkersWithPylon = new PlacementProfile(
    "Hug workers with pylon",
    preferPowering              = 0.1,
    preferCoveringWorkers       = 1.0,
    avoidDistanceFromEnemy      = 0.00001)
  
  val hugWorkersWithCannon = new PlacementProfile(
    "Hug workers with cannon",
    preferCoveringWorkers       = 1.0,
    avoidDistanceFromEnemy      = 0.00001)
  
  val cannonAgainstAir = new PlacementProfile(
    "Cannon against air/drops",
    preferCoveringWorkers       = 1.0,
    preferDistanceFromEnemy     = 0.5)
}
