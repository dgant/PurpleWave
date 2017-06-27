package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone          = 5.0,
    preferGas           = 0.0,
    preferSpace         = 0.1,
    preferPowering      = 0.25,
    preferEnemyDistance = 1.0,
    avoidDistance       = 2.5,
    avoidExitDistance   = 1.0
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone          = 1.0,
    preferGas           = 0.0,
    preferSpace         = 0.5,
    preferPowering      = 0.0,
    preferEnemyDistance = 0.0,
    avoidDistance       = 0.25,
    avoidExitDistance   = 4.0
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone          = 1.0,
    preferGas           = 0.0,
    preferSpace         = 0.0,
    preferPowering      = 0.0,
    preferEnemyDistance = 4.0,
    avoidDistance       = 1.0,
    avoidExitDistance   = 0.0
  )
  
  val gas = new PlacementProfile(
    "Gas",
    preferZone          = 100.0,
    preferGas           = 0.0,
    preferSpace         = 0.0,
    preferPowering      = 0.0,
    preferEnemyDistance = 1.0,
    avoidDistance       = 1.0,
    avoidExitDistance   = 0.0
  )
  
  val townHall = new PlacementProfile(
    "Town Hall",
    preferZone          = 0.0,
    preferGas           = 1.0,
    preferSpace         = 0.0,
    preferPowering      = 0.0,
    preferEnemyDistance = 1.5,
    avoidDistance       = 2.5,
    avoidExitDistance   = 0.0
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
    else
      tech
  }
}
