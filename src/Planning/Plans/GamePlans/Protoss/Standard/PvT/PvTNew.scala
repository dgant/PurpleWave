package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.GamePlans.GameplanImperative
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.DoQueue

class PvTNew extends GameplanImperative {

  var goObs: Boolean = true
  var goReavers: Boolean = true
  var goCarriers: Boolean = false
  var goArbiters: Boolean = true

  def doEmergencyReactions(): Unit = {
    // Vs. BBS: Zealots into 3-Gate Goon
    // Vs. 10-12: Zealots into 3-Gate Goon
    // Vs. Worker rush: Zealots into 3-Gate Goon

    Vector(new PvTIdeas.ReactToRaxCheese, new PvTIdeas.ReactToBunkerRush, new PvTIdeas.ReactToWorkerRush).foreach(_.update())
  }

  def openNexusFirst(): Unit = {

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

  def openZZCoreZ(): Unit = {

  }

  def open24Nexus(): Unit = {

  }

  def open3Gate(): Unit = {

  }

  def open4Gate(): Unit = {

  }

  def openReaver(): Unit = {

  }

  def openDT(): Unit = {

  }

  override def executeBuild(): Unit = {
    // TODO: Expansion is informative
    // TODO: 10-12 is informative
    // TODO: 14 CC is informative
    // TODO: 1 Rax FE is informative
    // TODO: Fac is informative
    // TODO: 2 Fac is informative
    // TODO: 3 Fac is informative
    // TODO: 3rd CC is informative
    // TODO: Early turret is informative
    // TODO: Factory count is informative
    // TODO: Armory count is informative
    // TODO: Bio is informative
    // TODO: Early Vulture is informative
    // TODO: Early Vulture Speed is informative
    // TODO: Early Vulture Mines is informative
    // TODO: Early Siege is informative
    // TODO: Goliath is informative
    // TODO: Dropship is informative

    // TODO: Take/Retake natural when?
    // TODO: 2/3 Gateway when?
    // TODO: When Reaver/skip?
    // TODO: Third when?
    // TODO: Fourth when?
    // TODO: If we take mineral 3rd, get speed+ups
    // TODO: Carriers when?
    // TODO: Storm when?
    // TODO: Double upgrades when?

    // Early vulture -> Prefer obs
    // 14CC -> ???
    // 1 Rax -> Reaver, Fast 3rd
    // Siege expand -> Fast 3rd
    // Early Vulture -> Obs, Reaver
    // 2/3 Fac -> Prefer muscle
    // 3rd CC -> Fast 4th
    // Bio -> Reaver, 3rd, upgrades, 4th, double upgrades

    if (false) {
      openNexusFirst()
    } else if (false) {
      open1ZealotExpand()
    } else if (false) {
      openZZCoreZ()
    } else if (false) {
      open24Nexus()
    } else if (false) {
      open3Gate()
    } else if (false) {
      open4Gate()
    } else if (false) {
      openReaver()
    } else if (false) {
      openDT()
    } else {
      openZZCoreZ() // A safe default
    }
  }

  override def executeMain(): Unit = {
    val observers = new DoQueue(doObserver)
    val emergencyReavers = new DoQueue(doEmergencyReaver)
    val reavers = new DoQueue(doReaver)
    val shuttles = new DoQueue(doShuttles)
    val storm = new DoQueue(doStorm)
    val stasis = new DoQueue(doStasis)
    val recall = new DoQueue(doRecall)
    val carrier = new DoQueue(doCarriers)
    val singleUpgrades = new DoQueue(doSingleUpgrades)
    val doubleUpgrades = new DoQueue(doDoubleUpgrades)

    get(Protoss.Pylon)
    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(2, Protoss.Nexus)
    if (enemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.bio, With.fingerprints.oneRaxFE, With.fingerprints.fourteenCC)) {
      reavers()
    } else {
      observers()
    }
    get(3, Protoss.Gateway)
    requireMiningBases(2)
    get((1 + enemies(Terran.Factory)) * 3 / 2, Protoss.Gateway)
    if (safeAtHome) get(3, Protoss.Nexus)
    get(6, Protoss.Gateway)
    singleUpgrades()
    storm()
    requireMiningBases(3)
    get(9, Protoss.Gateway)
    get(4, Protoss.Nexus)
    shuttles()
    stasis()
    requireMiningBases(4)
    get(14, Protoss.Gateway)
    doubleUpgrades()
    requireMiningBases(5)
    get(22, Protoss.Gateway)
    requireMiningBases(6)
    get(30, Protoss.Gateway)
  }

  def doEmergencyReaver(): Unit = {
    get(Protoss.RoboticsFacility)
    get(Protoss.RoboticsSupportBay)
  }

  def doReaver(): Unit = {
    doShuttles()
    if (units(Protoss.Reaver) > 2) {
      get(Protoss.ScarabDamage)
    }
  }

  def doShuttles(): Unit = {
    get(Protoss.RoboticsFacility)
    buildOrder(Get(Protoss.Shuttle))
    get(Protoss.RoboticsSupportBay)
    get(Protoss.ShuttleSpeed)
  }


  def doObserver(): Unit = {
    get(Protoss.RoboticsFacility)
    get(Protoss.Observatory)
    if (enemyHasShown(Terran.SpiderMine) || enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 1 || goCarriers || gasPumps > 2) {
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
    if (techComplete(Protoss.PsionicStorm) && gasPumps > 2) get(Protoss.HighTemplarEnergy)
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
    if (gasPumps > 2) get(2, Protoss.Stargate)
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
