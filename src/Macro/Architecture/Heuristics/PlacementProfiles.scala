package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone      = 3.0,
    preferExit      = 0.75,
    preferGas       = 0.0,
    preferSpace     = 0.5,
    preferPowering  = 6.0,
    avoidDistance   = 1.0,
    avoidEnemy      = 0.5
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone      = 1.0,
    preferExit      = 2.0,
    preferGas       = 0.0,
    preferSpace     = 1.0,
    preferPowering  = 0.0,
    avoidDistance   = 0.0,
    avoidEnemy      = 0.25
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone      = 1.0,
    preferExit      = 0.0,
    preferGas       = 0.0,
    preferSpace     = 0.0,
    preferPowering  = 0.0,
    avoidDistance   = 0.0,
    avoidEnemy      = 1.0
  )
  
  val gas  = new PlacementProfile(
    "Gas",
    preferZone      = 100.0,
    preferExit      = 0.0,
    preferGas       = 0.0,
    preferSpace     = 0.0,
    preferPowering  = 0.0,
    avoidDistance   = 1.0,
    avoidEnemy      = 1.0
  )
  
  val townHall = new PlacementProfile(
    "Town Hall",
    preferZone      = 0.0,
    preferExit      = 0.0,
    preferGas       = 2.0,
    preferSpace     = 0.0,
    preferPowering  = 0.0,
    avoidDistance   = 4.0,
    avoidEnemy      = 1.0
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
