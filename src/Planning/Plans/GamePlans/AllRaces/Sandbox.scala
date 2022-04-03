package Planning.Plans.GamePlans.AllRaces

import Macro.Requests.Get
import Planning.Plans.GamePlans.GameplanImperative
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanImperative {
  override def executeBuild(): Unit = {
    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(11, Protoss.Probe),
      Get(Protoss.Forge),
      Get(13, Protoss.Probe),
      Get(Protoss.PhotonCannon),
      Get(15, Protoss.Probe),
      Get(2, Protoss.PhotonCannon),
      Get(16, Protoss.Probe),
      Get(3, Protoss.PhotonCannon),
      Get(2, Protoss.Pylon),
      Get(17, Protoss.Probe))
  }
  override def executeMain(): Unit = {
    pump(Protoss.Observer, 1)
    pumpShuttleAndReavers(4)
    pump(Protoss.DarkTemplar, 3)
    pump(Protoss.HighTemplar, 24)
    pump(Protoss.Carrier)
    if (units(Protoss.HighTemplar) >= 2) {
      get(Protoss.PsionicStorm)
    }
    if (units(Protoss.Carrier) >= 3) {
      upgradeContinuously(Protoss.CarrierCapacity)
      upgradeContinuously(Protoss.AirArmor)
      if (upgradeComplete(Protoss.AirArmor, level = 3)) upgradeContinuously(Protoss.AirDamage)
    }
    get(4, Protoss.PhotonCannon)
    get(Protoss.Gateway)
    get(8, Protoss.PhotonCannon)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    get(14, Protoss.PhotonCannon)
    get(3, Protoss.Gateway)
    pump(Protoss.PhotonCannon, 20)
    buildGasPumps()
    pump(Protoss.Zealot)
    get(6, Protoss.Gateway)
    upgradeContinuously(Protoss.GroundDamage)
    if (upgradeComplete(Protoss.GroundDamage, 3)) { upgradeContinuously(Protoss.GroundArmor) }
    get(Protoss.ZealotSpeed)
    get(Protoss.HighTemplarEnergy)
    get(2, Protoss.Stargate)
    get(Protoss.FleetBeacon)
    pump(Protoss.Zealot)
    get(8, Protoss.Gateway)
  }
  /*
  override def executeBuild(): Unit = {
    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(14, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(15, Protoss.Probe),
      Get(2, Protoss.Pylon),
      Get(16, Protoss.Probe),
      Get(Protoss.CitadelOfAdun),
      Get(19, Protoss.Probe),
      Get(3, Protoss.Gateway),
      Get(Protoss.TemplarArchives),
      Get(20, Protoss.Probe),
      Get(3, Protoss.Pylon),
      Get(21, Protoss.Probe),
      Get(3, Protoss.DarkTemplar))
  }
  var madeDTs = false
  override def executeMain(): Unit = {
    if (unitsComplete(Protoss.Dragoon) > 0) {
      attack()
    } else {
      With.blackboard.darkTemplarHarass.set(false)
    }
    if (unitsComplete(MatchWarriors) >= 10) requireMiningBases(2)
    if (unitsComplete(MatchWarriors) >= 20) requireMiningBases(3)
    if (enemyShownCloakedThreat) {
      get(Protoss.RoboticsFacility)
      get(Protoss.Observatory)
    }
    pump(Protoss.Observer, 2)
    pump(Protoss.DarkTemplar, 3)
    buildOrder(Get(5, Protoss.DarkTemplar))
    madeDTs ||= units(Protoss.DarkTemplar) >= 3
    if (madeDTs) {
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
      get(3, Protoss.Gateway)
      get(2, Protoss.Nexus)
      get(6, Protoss.Gateway)
      buildGasPumps()
      get(12, Protoss.Gateway)
    }
  }
  */
}
