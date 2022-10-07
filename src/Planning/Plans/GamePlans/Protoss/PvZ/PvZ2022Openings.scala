package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Planning.Plans.GamePlans.All.GameplanImperative
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.Time.GameTime
import Utilities.UnitFilters.IsWarrior

abstract class PvZ2022Openings extends GameplanImperative {

  protected def reactVs4Pool(): Unit = {
    if (enemyStrategy(With.fingerprints.fourPool) && unitsComplete(IsWarrior) < 5) {
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(9, Protoss.Probe)
      if (units(Protoss.Forge) > 0) {
        get(3, Protoss.PhotonCannon)
      }
      once(Protoss.Gateway)
      pumpSupply()
      pumpWorkers()
      pump(Protoss.Zealot)
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
    }
  }
  protected def open910(): Unit = {
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
  protected def open1012(): Unit = {
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
  }
  protected def openZZCoreZ(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    scoutOn(Protoss.Gateway)
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
    once(13, Protoss.Probe)
    once(2,  Protoss.Nexus)
    once(Protoss.Zealot)
    once(15, Protoss.Probe)
    once(2,  Protoss.Gateway)
  }

  private val speedlingStrategies = Vector(With.fingerprints.fourPool, With.fingerprints.ninePoolGas, With.fingerprints.ninePoolHatch, With.fingerprints.overpoolGas, With.fingerprints.tenHatchPoolGas, With.fingerprints.twoHatchMain, With.fingerprints.oneHatchGas)
  protected def anticipateSpeedlings: Boolean = {
    var output = enemyStrategy(speedlingStrategies: _*)
    output ||= enemyRecentStrategy(speedlingStrategies: _*) && ! With.scouting.enemyMainFullyScouted
    output ||= enemiesShown(Zerg.Zergling) > 10 && With.frame < GameTime(4, 0)()
    output ||= enemiesShown(Zerg.Zergling) > 12 && With.frame < GameTime(4, 30)()
    output ||= enemyHasUpgrade(Zerg.ZerglingSpeed)
    output &&= ! enemyHydralisksLikely
    output &&= ! enemyMutalisksLikely
    output &&= ! enemyLurkersLikely
    output
  }
}
