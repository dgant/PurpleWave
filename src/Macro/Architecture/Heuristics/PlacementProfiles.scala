package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone      = 3.0,
    preferExit      = 0.5,
    preferGas       = 0.0,
    preferSpace     = 1.0,
    preferPowering  = 8.0,
    avoidExit       = 0.0,
    avoidDistance   = 1.0,
    avoidEnemy      = 1.0
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone      = 1.0,
    preferExit      = 1.0,
    preferGas       = 0.0,
    preferSpace     = 1.0,
    preferPowering  = 0.0,
    avoidExit       = 0.0,
    avoidDistance   = 0.0,
    avoidEnemy      = 0.0
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone      = 1.0,
    preferExit      = 0.0,
    preferGas       = 0.0,
    preferSpace     = 1.0,
    preferPowering  = 0.0,
    avoidExit       = 1.0,
    avoidDistance   = 0.0,
    avoidEnemy      = 0.0
  )
  
  val gas  = new PlacementProfile(
    "Gas",
    preferZone      = 100.0,
    preferExit      = 0.0,
    preferGas       = 0.0,
    preferSpace     = 0.0,
    preferPowering  = 0.0,
    avoidExit       = 0.0,
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
    avoidExit       = 0.0,
    avoidDistance   = 2.0,
    avoidEnemy      = 1.5
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
