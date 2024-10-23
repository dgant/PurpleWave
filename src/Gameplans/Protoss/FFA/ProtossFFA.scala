package Gameplans.Protoss.FFA

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.?
import Utilities.UnitFilters.{IsGroundWarrior, IsWarrior}

class ProtossFFA extends GameplanImperative {

  val expandEver = true

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(15, Protoss.Probe)
    once(2, Protoss.Zealot)

  }

  override def executeMain(): Unit = {
    scoutOn(Protoss.Pylon)
    if (expandEver) {
      requireMiningBases(2)
      if (unitsComplete(Protoss.Carrier)  >= 4)   requireMiningBases(3)
      if (unitsComplete(IsWarrior)        >= 12)  requireMiningBases(3)
      if (unitsComplete(Protoss.Carrier)  >= 6)   requireMiningBases(4)
      if (unitsComplete(IsWarrior)        >= 20)  requireMiningBases(4)
    }
                                                pump(Protoss.Observer, 2)
                                                pump(Protoss.Arbiter, 2)
                                                pump(Protoss.Carrier)
    if (units(Protoss.Carrier)      >= 2)       upgradeContinuously(Protoss.CarrierCapacity)
    if (units(Protoss.Carrier)      >= 2)       upgradeContinuously(Protoss.AirDamage)    && upgradeContinuously(Protoss.AirArmor)
    if (units(Protoss.Arbiter)      >= 2)       get(Protoss.Stasis)
    if (units(IsGroundWarrior)      >= 24)      upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    if (supplyUsed200               >= 100)     upgradeContinuously(Protoss.Shields)
    if (units(Protoss.HighTemplar)  >= 2)       get(Protoss.PsionicStorm)
    if (enemyShownCloakedThreat)                upgradeContinuously(Protoss.ObserverSpeed)
                                                pump(Protoss.DarkTemplar, 1)
                                                pump(Protoss.HighTemplar, 3)
    if (upgradeStarted(Protoss.ShuttleSpeed))   pump(Protoss.Shuttle, 1)
    if (upgradeStarted(Protoss.ZealotSpeed))    pump(Protoss.Zealot, 6)
                                                upgradeContinuously(Protoss.DragoonRange)
                                                pump(Protoss.Dragoon, 24, maximumConcurrently = ?(upgradeStarted(Protoss.CarrierCapacity), Int.MaxValue, 2))
                                                pump(Protoss.HighTemplar, 4)
                                                pump(Protoss.Zealot, 12)
    makeArchons(50)

    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(3, Protoss.Gateway)
    if (gas < 800 && With.units.ours.filter(Protoss.Assimilator).forall(_.complete) && units(Protoss.Assimilator) < units(Protoss.Probe) / 5) {
      pumpGasPumps()
    }
    get(2, Protoss.Stargate)
    get(Protoss.FleetBeacon)
    get(Protoss.AirDamage)
    get(4, Protoss.Gateway)
    upgradeContinuously(Protoss.AirDamage) && upgradeContinuously(Protoss.AirArmor)
    get(Protoss.CitadelOfAdun)
    get(Protoss.Zealot)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    get(Protoss.Forge)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.TemplarArchives)
    get(Protoss.PsionicStorm)
    get(Protoss.ArbiterTribunal)
    get(3, Protoss.Stargate)

    pump(Protoss.HighTemplar)
    pump(Protoss.Zealot)
    get(15, Protoss.Gateway)
  }
}
