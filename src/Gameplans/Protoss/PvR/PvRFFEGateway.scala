package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Protoss

class PvRFFEGateway extends GameplanImperative {

  override def executeBuild():  Unit = {
    scoutOn(Protoss.Pylon)
    if (With.geography.startLocations.length >= 4) {
      scoutOn(Protoss.Forge, scouts = 2)
    }
    once(8, Protoss.Probe)
    tryBuildInWall(1, Protoss.Pylon)
    once(13, Protoss.Probe)
    if (enemyStrategy(With.fingerprints.hatchFirst, With.fingerprints.nexusFirst)) {
      requireMiningBases(2)
      once(14, Protoss.Probe)
      get(Protoss.Gateway)
      once(15, Protoss.Probe)
    }
    tryBuildInWall(1, Protoss.Forge)
    once(14, Protoss.Probe)
    tryBuildInWall(2, Protoss.PhotonCannon)
    once(15, Protoss.Probe)
    requireMiningBases(2)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
    once(16, Protoss.Probe)
    tryBuildInWall(1, Protoss.Gateway)
    once(17, Protoss.Probe)
  }

  override def executeMain(): Unit = {
    attack(unitsComplete(Protoss.Carrier) >= 4 && unitsComplete(Protoss.Interceptor) >= 24 && safePushing)
    tryBuildInWall(3, Protoss.PhotonCannon)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    tryBuildInWall(4, Protoss.PhotonCannon)
    once(4, Protoss.Carrier)
    get(Protoss.CarrierCapacity)
    pump(Protoss.Observer, 2)
    pump(Protoss.Carrier)
    get(2, Protoss.Stargate)
    tryBuildInWall(6, Protoss.PhotonCannon)
    get(Protoss.FleetBeacon)
    upgradeContinuously(Protoss.AirArmor)
    tryBuildInWall(8, Protoss.PhotonCannon)
    upgradeContinuously(Protoss.AirDamage)
    get(Protoss.RoboticsFacility, Protoss.Observatory)
    get(4, Protoss.Stargate)
    pumpGasPumps()
    requireMiningBases(3)
    buildCannonsAtExpansions(6, PlaceLabels.DefendHall)
    get(6, Protoss.Stargate)
  }
}
