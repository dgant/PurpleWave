package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import ProxyBwapi.Races.{Protoss, Terran}

class PvTNew extends GameplanImperative {

  def doEmergencyReactions(): Unit = {
    Vector(new PvTIdeas.ReactToRaxCheese, new PvTIdeas.ReactToBunkerRush, new PvTIdeas.ReactToWorkerRush).foreach(_.update())
  }

  def open1ZealotExpand(): Unit = {
    // Reference: https://youtu.be/jYRHZVAjhX8?t=4932
    buildOrder(Get(8, Protoss.Probe))
    get(Protoss.Pylon)
    buildOrder(Get(10, Protoss.Probe))
    get(Protoss.Gateway)
    buildOrder(Get(13, Protoss.Probe))
    buildOrder(Get(Protoss.Zealot))
    buildOrder(Get(14, Protoss.Probe))
    doEmergencyReactions()
    get(2, Protoss.Nexus)
    scoutOn(Protoss.Nexus, quantity = 2)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    buildOrder(Get(15, Protoss.Probe))
    buildOrder(Get(2, Protoss.Zealot))
    buildOrder(Get(17, Protoss.Probe))
    get(2, Protoss.Pylon)
    buildOrder(Get(Protoss.Dragoon))
  }

  override def executeBuild(): Unit = {
    open1ZealotExpand()
  }

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
    if (units(Protoss.Reaver) > 2) {
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
    if (gasPumps > 2) get(2, Protoss.Stargate)
  }

  def doCarriers(): Unit = {
    get(2, Protoss.Stargate)
    get(Protoss.FleetBeacon)
    buildOrder(Get(2, Protoss.Carrier))
    if (enemyStrategy(With.fingerprints.bio)) upgradeContinuously(Protoss.AirArmor) else upgradeContinuously(Protoss.AirDamage)
    get(Protoss.CarrierCapacity)
    if (upgradeComplete(Protoss.AirArmor, 3))  upgradeContinuously(Protoss.AirDamage)
    if (upgradeComplete(Protoss.AirDamage, 3))  upgradeContinuously(Protoss.AirArmor)
    if (units(Protoss.Carrier) > 3 && miningBases > 2) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
    }
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
    new PvTIdeas.TrainArmy().update()
  }

  def doExpand(): Unit = {

  }
}
