package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone                  = 10.0,
    preferSpace                 = 0.1,
    preferPowering              = 0.25,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 0.25,
    avoidDistanceFromIdealRange = 0.1
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone                  = 1.0,
    preferSpace                 = 0.5,
    avoidDistanceFromBase       = 2.0,
    avoidDistanceFromIdealRange = 2.0
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone                  = 1.0,
    preferDistanceFromEnemy     = 3.0,
    avoidDistanceFromBase       = 1.0)
  
  val gas = new PlacementProfile(
    "Gas",
    preferZone                  = 100.0,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 1.0)
  
  val townHall = new PlacementProfile(
    "Town Hall",
    preferNatural               = 6.0,
    preferGas                   = 1.0,
    preferDistanceFromEnemy     = 1.5,
    avoidDistanceFromBase       = 2.0)
  
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
    avoidSurfaceArea            = 0.25,
    avoidDistanceFromIdealRange = 5.0)
  
  val mineralCannon = new PlacementProfile(
    "Pylon for mineral line Cannons",
    preferZone                  = 100.0,
    preferPowering              = 0.1,
    preferCoveringWorkers       = 1.0)
  
  val wall = new PlacementProfile(
    "Ground defense",
    preferNatural               = 30.0,
    preferCoveringWorkers       = 2.0,
    avoidDistanceFromEnemy      = 0.75,
    avoidDistanceFromBase       = 0.5,
    avoidSurfaceArea            = 1.0,
    avoidDistanceFromIdealRange = 1.0)
  
  var proxy = new PlacementProfile(
    "Proxy",
    preferZone                  = 5.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 4.0)
  
  var proxyPylon = new PlacementProfile(
    "Proxy Pylon",
    preferZone                  = 5.0,
    preferPowering              = 5.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0)
  
  val hugTheNexus = new PlacementProfile(
    "Hugging Nexus",
    preferPowering        = 0.25,
    avoidDistanceFromBase = 1.0,
    preferCoveringWorkers = 0.25)
  
  def default(blueprint: Blueprint): PlacementProfile = {
    if (blueprint.townHall)
      townHall
    else if (blueprint.gas)
      gas
    else if (blueprint.powers)
      pylon
    else if (blueprint.building.exists(_.trainsGroundUnits))
      factory
    else if (blueprint.building.exists(_.canAttack) || blueprint.wall)
      wall
    else
      tech
  }
}
