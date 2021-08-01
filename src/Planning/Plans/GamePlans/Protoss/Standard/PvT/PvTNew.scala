package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import ProxyBwapi.Races.{Protoss, Terran}

class PvTNew extends GameplanImperative {
  override def executeMain(): Unit = {
    get(Protoss.Pylon)
    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)

    // TODO: Retake natural when?
    // TODO: 2/3 Gateway when?
    // TODO: When Reaver/skip?
    // TODO: Third when?
    // TODO: Fourth when?
    // TODO: If we take mineral 3rd, get speed+ups
    // Carriers vs. heavy vulture/2fac commitment
  }

  def doTech(): Unit = {

  }

  def doReaver(): Unit = {
    get(Protoss.RoboticsFacility)
    buildOrder(Get(Protoss.Shuttle))
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
    if (units(Protoss.Reaver) > 1) {
      get(Protoss.ScarabDamage)
    }
  }

  def doObserver(): Unit = {
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    if (enemyHasShown(Terran.SpiderMine)) {
      get(Protoss.ObserverSpeed)
      if (upgradeComplete(Protoss.ObserverSpeed)) {
        get(Protoss.ObserverVisionRange)
      }
    }
  }

  def doStorm(): Unit = {
    get(Protoss.CitadelOfAdun)
    buildGasPumps()
    get(Protoss.TemplarArchives)
    get(Protoss.PsionicStorm)
    if (techComplete(Protoss.PsionicStorm)) get(Protoss.HighTemplarEnergy)
  }

  def doSingleUpgrades(): Unit = {
    get(Protoss.Forge)
    get(Protoss.GroundDamage)
    get(Protoss.CitadelOfAdun)
    buildGasPumps()
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)
    upgradeContinuously(Protoss.GroundDamage)
    if (upgradeComplete(Protoss.GroundDamage)) {
      upgradeContinuously(Protoss.GroundArmor)
    }
  }

  def doDoubleUpgrades(): Unit = {
    get(2, Protoss.Forge)
    get(Protoss.GroundDamage)
    get(Protoss.GroundArmor)
    get(Protoss.CitadelOfAdun)
    buildGasPumps()
    get(Protoss.ZealotSpeed)
    get(Protoss.TemplarArchives)
    upgradeContinuously(Protoss.GroundDamage)
    upgradeContinuously(Protoss.GroundArmor)
  }

  def doArbiters(): Unit = {
    get(Protoss.CitadelOfAdun)
    buildGasPumps()
    get(Protoss.Stargate)
    get(Protoss.TemplarArchives)
    get(Protoss.ArbiterTribunal)
    if (techComplete(Protoss.Stasis) || techComplete(Protoss.Recall)) get(Protoss.ArbiterEnergy)
  }

  def doCarriers(): Unit = {
    get(2, Protoss.Stargate)
    get(Protoss.FleetBeacon)
    buildOrder(Get(2, Protoss.Carrier))
    if (enemyStrategy(With.fingerprints.bio)) upgradeContinuously(Protoss.AirArmor) else upgradeContinuously(Protoss.AirDamage)
    get(Protoss.CarrierCapacity)
    if (upgradeComplete(Protoss.AirArmor, 3))  upgradeContinuously(Protoss.AirDamage)
    if (upgradeComplete(Protoss.AirDamage, 3))  upgradeContinuously(Protoss.AirArmor)
  }

  def doStasis(): Unit = {
    doArbiters()
    get(Protoss.Stasis)
  }

  def doRecall(): Unit = {
    doArbiters()
    get(Protoss.Recall)
  }

  def doTrainArmy(): Unit = {

  }

  def doExpand(): Unit = {

  }
}
