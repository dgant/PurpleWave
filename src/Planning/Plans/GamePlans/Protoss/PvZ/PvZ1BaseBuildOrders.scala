package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Planning.Plans.GamePlans.All.GameplanImperative
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitFilters.{IsHatchlike, IsWarrior}

abstract class PvZ1BaseBuildOrders extends GameplanImperative {

  protected def reactVs4Pool(): Unit = {
    if (With.fingerprints.fourPool() && unitsComplete(IsWarrior) < 5) {
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(9, Protoss.Probe)
      if (have(Protoss.Forge)) {
        get(3, Protoss.PhotonCannon)
      }
      once(Protoss.Gateway)
      pumpSupply()
      pumpWorkers()
      pump(Protoss.Zealot)
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
      get(2, Protoss.Gateway)
    }
  }
  protected def openVsSpeedlings(): Unit = {
    if (unitsComplete(Protoss.Gateway) < 3) {
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Gateway)
    once(7, Protoss.Zealot)
    once(4, Protoss.Pylon)
    once(19, Protoss.Probe)
    once(10, Protoss.Zealot)
    once(21, Protoss.Probe)
    once(13, Protoss.Zealot)
  }
  protected def open910(): Unit = {
    if (unitsEver(Protoss.Gateway) < 2) {
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    once(Protoss.Gateway)
    once(10, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(11, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(2, Protoss.Zealot)
    once(12, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(13, Protoss.Probe)
    once(4, Protoss.Zealot)
    once(14, Protoss.Probe)
    reactVs4Pool()
    once(3, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(4, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(7, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(5, Protoss.Pylon)
    once(19, Protoss.Probe)
  }
  protected def open1012Gas(): Unit = {
    if (unitsEver(Protoss.Gateway) < 2) {
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(4, Protoss.Pylon)
    once(Protoss.Assimilator)
    once(19, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(7, Protoss.Zealot)
    once(21, Protoss.Probe)
  }
  protected def open1012Expand(): Unit = {
    if (unitsEver(Protoss.Gateway) < 2) {
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(17, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(2, Protoss.Nexus)
    once(19, Protoss.Probe)
    once(7, Protoss.Zealot)
  }

  protected def openCoreZ(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(Protoss.Assimilator)
    once(14, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
  }
  protected def openZCoreZ(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(Protoss.Assimilator)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(16, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(19, Protoss.Probe)
  }
  protected def openZZCoreZ(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(Protoss.Assimilator)
    once(15, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
  }
  protected def openGateNexus(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)

    if (With.fingerprints.hatchFirst()) {
      once(13, Protoss.Probe)
      once(2,  Protoss.Nexus)
      once(Protoss.Zealot)
      once(14, Protoss.Probe)
      once(2, Protoss.Pylon)
      once(15, Protoss.Probe)
      once(2, Protoss.Zealot)
      once(16, Protoss.Probe)
      once(2,  Protoss.Gateway)
      once(17, Protoss.Probe)
      once(3, Protoss.Zealot)
      once(18, Protoss.Probe)
    } else {
      once(12, Protoss.Probe)
      once(2, Protoss.Pylon)
      once(13, Protoss.Probe)
      once(1, Protoss.Zealot)
      once(15, Protoss.Probe)
      once(2, Protoss.Zealot)
      once(17, Protoss.Probe)
      once(2, Protoss.Nexus)
      once(18, Protoss.Probe)
      once(3, Protoss.Zealot)
    }
  }
  protected def openNexusFirst(): Unit = {
    if (unitsEver(Protoss.Nexus) < 2) {
      cancel(Protoss.Assimilator, Protoss.Gateway)
    }
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(13, Protoss.Probe)
    once(2, Protoss.Nexus)
    once(Protoss.Gateway)
    once(15, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(17, Protoss.Probe)
    once(Protoss.Assimilator)
    once(Protoss.Zealot)
    once(Protoss.CyberneticsCore)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
  }

  private val speedlingStrategies = Vector(
    With.fingerprints.fourPool,
    With.fingerprints.ninePoolGas,
    With.fingerprints.overpoolGas,
    With.fingerprints.tenHatchPoolGas,
    With.fingerprints.twoHatchMain,
    With.fingerprints.oneHatchGas,
    With.fingerprints.speedBeforeLair)

  private var _previouslyAnticipatedSpeedlings: Boolean = false
  protected def anticipateSpeedlings: Boolean = {
    var output  =   With.fingerprints.zerglingsOnly.recently || enemyRecentStrategy(speedlingStrategies: _*)
    output      &&= ! (With.scouting.enemyMainFullyScouted && With.frame > Minutes(3)())
    output      ||= enemyStrategy(speedlingStrategies: _*)
    output      ||= enemiesShown(Zerg.Zergling) >= 20
    output      &&= enemies(Zerg.CreepColony, Zerg.SunkenColony) > 1
    output      ||= enemiesShown(Zerg.Zergling) >= 30
    output      ||= enemyHasShown(Zerg.Zergling) && enemies(Zerg.Drone) < enemies(IsHatchlike) * 5;
    output      ||= With.units.enemy.filter(Zerg.Extractor).exists(e => With.framesSince(e.completionFrame) > Seconds(21)() && ! With.units.enemy.exists(Zerg.LairOrHive)) // It takes 21 seconds to mine 100 gas from an Extractor
    output      &&= ! enemyHydralisksLikely
    output      &&= ! enemyMutalisksLikely
    output      &&= ! enemyLurkersLikely
    output      &&= (_previouslyAnticipatedSpeedlings || units(Protoss.Gateway, Protoss.CyberneticsCore, Protoss.CitadelOfAdun, Protoss.RoboticsFacility, Protoss.Stargate) < 3) // At some point, stop reacting to speedlings
    _previouslyAnticipatedSpeedlings = output
    output
  }
}
