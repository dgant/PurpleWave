package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone                  = 3.0,
    preferSpace                 = 0.1,
    preferPowering              = 0.5,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 2.5,
    avoidDistanceFromExitRange  = 1.0
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone                  = 1.0,
    preferSpace                 = 0.5,
    preferPowering              = 0.0,
    preferDistanceFromEnemy     = 0.0,
    avoidDistanceFromBase       = 0.25,
    avoidDistanceFromExitRange  = 4.0
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone                  = 1.0,
    preferDistanceFromEnemy     = 4.0,
    avoidSurfaceArea            = 0.5,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromExitRange  = 0.0
  )
  
  val gas = new PlacementProfile(
    "Gas",
    preferZone                  = 100.0,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 1.0
  )
  
  val townHall = new PlacementProfile(
    "Town Hall",
    preferGas                   = 1.0,
    preferDistanceFromEnemy     = 1.5,
    avoidDistanceFromBase       = 2.5
  )
  
  val cannonPylon = new PlacementProfile(
    "Pylon for Cannons",
    preferPowering              = 1.0,
    preferDistanceFromEnemy     = 0.5,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 1.0,
    avoidSurfaceArea            = 0.5,
    avoidDistanceFromExitRange  = 2.0
  )
  
  val groundDefense = new PlacementProfile(
    "Ground defense",
    preferCoveringWorkers       = 1.0,
    avoidDistanceFromBase       = 0.5,
    avoidSurfaceArea            = 0.5,
    avoidDistanceFromExitRange  = 2.0
  )
  
  def default(buildingDescriptor: BuildingDescriptor): PlacementProfile = {
    if (buildingDescriptor.townHall)
      townHall
    else if (buildingDescriptor.gas)
      gas
    else if (buildingDescriptor.powers)
      pylon
    else if (buildingDescriptor.margin)
      factory
    else if (buildingDescriptor.attackRange.isDefined)
      groundDefense
    else
      tech
  }
}
