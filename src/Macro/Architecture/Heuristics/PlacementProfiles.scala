package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object PlacementProfiles {
  
  def default(blueprint: Blueprint): PlacementProfile = {
    if (blueprint.requireTownHallTile.get) {
      if (With.blackboard.preferCloseExpansion.get)
        townHallNearby
      else if (With.self.isZerg || With.enemies.forall(_.isTerran))
        townHallFar
      else
        townHallNearby
    }
    else if (blueprint.requireGasTile.get)
      gas
    else if (blueprint.powers.get)
      pylon
    else if (blueprint.building.contains(Protoss.RoboticsFacility))
      robo
    else if (blueprint.building.exists(_.trainsGroundUnits)) {
      if (blueprint.building.contains(Terran.Barracks))
        factoryNoSpace
      else
        factory
    }
    else if (blueprint.building.exists(building => building.attacks || building == Zerg.CreepColony || building == Protoss.ShieldBattery))
      defensive
    else
      tech
  }
  
  val basic = new PlacementProfile(
    "Basic",
    preferZone                  = 1.0,
    preferNatural               = 0.0,
    preferResources             = 0.0,
    preferRhythm                = 1.0,
    preferSpace                 = 0.2,
    preferPowering              = 0.15,
    preferDistanceFromEnemy     = 0.6,
    preferSurfaceArea           = 0.25,
    avoidDistanceFromBase       = 0.3,
    avoidDistanceFromEnemy      = 0.0,
    avoidDistanceFromIdealRange = 0.05,
    avoidSurfaceArea            = 0.0
  )
  
  val pylon = new PlacementProfile("Pylon", basic)
  val robo = new PlacementProfile("Robo", basic) {
    avoidDistanceFromIdealRange = 6.0
  }
  val factory = new PlacementProfile("Factory", basic) {
    avoidSurfaceArea = 0.01
  }
  val factoryNoSpace = new PlacementProfile("Factory", basic) {
    preferSpace = 0.0
  }
  
  val tech = new PlacementProfile("Tech", basic) {
    preferZone                  = 1.0
    preferDistanceFromEnemy     = 1.0
    avoidDistanceFromIdealRange = 0.0
  }
  
  val gas = new PlacementProfile("Gas",
    preferWorkers               = 1.0,
    preferDistanceFromEnemy     = 1.0
  )
  
  val townHallNearby = new PlacementProfile("Town Hall Nearby",
    preferZone                  = 1.0,
    preferNatural               = 10.0,
    preferResources             = 0.75,
    preferRhythm                = 0.0,
    preferDistanceFromEnemy     = 2.0,
    avoidDistanceFromBase       = 1.0
  )
  
  val townHallFar = new PlacementProfile("Town Hall Far",
    preferZone                  = 1.0,
    preferNatural               = 10.0,
    preferResources             = 0.5,
    preferRhythm                = 0.0,
    preferDistanceFromEnemy     = 4.0,
    avoidDistanceFromBase       = 0.5
  )
  
  //////////////////////////
  // Specialty placements //
  //////////////////////////
  
  val backPylon = new PlacementProfile("Pylon for the back of your base", basic) {
    preferDistanceFromEnemy     = 1.0
    avoidDistanceFromIdealRange = 0.0
  }
  
  val defensive = new PlacementProfile(
    "Forge wall",
    preferZone                  = 100.0,
    preferNatural               = 10.0,
    preferPowering              = 0.4,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromIdealRange = 4.0)
  
  val proxyBuilding = new PlacementProfile(
    "Proxy",
    preferZone                  = 100.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val proxyPylon = new PlacementProfile(
    "Proxy Pylon",
    preferZone                  = 100.0,
    preferPowering              = 1.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)
  
  val proxyCannon = new PlacementProfile(
    "Proxy cannon",
    preferZone                  = 100.0,
    avoidDistanceFromEnemy      = 3.0,
    avoidSurfaceArea            = 1.0)

  val proxyTowardsEnemy = new PlacementProfile(
    "Proxy towards enemy",
    avoidDistanceFromEnemy = 1.0)

  val wallGathering = new PlacementProfile(
    "Wall gathering area",
    preferZone                  = 100.0,
    preferPowering              = 0.4,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromIdealRange = 4.0)

  val hugTownHall = new PlacementProfile(
    "Hug town hall",
    preferPowering              = 0.1,
    preferSpace                 = 2.0,
    avoidDistanceFromBase       = 1.0)

  val hugTownHallTowardsEntrance = new PlacementProfile(
    "Hug town hall towards entrance",
    preferPowering              = 0.1,
    preferSpace                 = 2.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEntrance   = 0.2)
  
  val hugWorkersWithPylon = new PlacementProfile(
    "Hug workers with pylon",
    preferPowering              = 0.1,
    avoidDistanceFromEnemy      = 0.00001)
  
  val hugWorkersWithCannon = new PlacementProfile(
    "Hug workers with cannon",
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 0.00001)
  
  val cannonAgainstAir = new PlacementProfile(
    "Cannon against air/drops",
    avoidDistanceFromBase       = 1.0,
    preferDistanceFromEnemy     = 0.5)
}
