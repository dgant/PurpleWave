package Planning.Plans.GamePlans.Protoss.PvZ

import Debugging.SimpleString
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plans.GamePlans.All.GameplanImperative
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.IsWarrior

abstract class PvZ1BaseOpenings extends GameplanImperative {

  protected trait   Opening       extends SimpleString
  protected object  Open910       extends Opening
  protected object  Open1012      extends Opening
  protected object  OpenZZCoreZ   extends Opening
  protected object  OpenGateNexus extends Opening

  protected var opening: Opening = Open1012

  protected def reactVs4Pool(): Unit = {
    if (With.fingerprints.fourPool() && unitsComplete(IsWarrior) < 5) {
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
      get(2, Protoss.Gateway)
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
    once(4, Protoss.Pylon)
    once(Protoss.Assimilator)
    once(19, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(7, Protoss.Zealot)
    once(21, Protoss.Probe)
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
  private var _previouslyAnticipatedSpeedlings: Boolean = false
  protected def anticipateSpeedlings: Boolean = {
    var output = enemyRecentStrategy(speedlingStrategies: _*)
    output &&= ! With.scouting.enemyMainFullyScouted && With.frame > Minutes(3)()
    output &&= ! enemyStrategy(With.fingerprints.overpool, With.fingerprints.twelvePool, With.fingerprints.twelveHatch)
    output ||= enemyStrategy(speedlingStrategies: _*)
    output ||= enemiesShown(Zerg.Zergling) > 10 && With.frame < GameTime(3, 15)()
    output ||= enemiesShown(Zerg.Zergling) > 12 && With.frame < GameTime(3, 30)()
    output ||= enemiesShown(Zerg.Zergling) > 16
    output ||= enemyHasUpgrade(Zerg.ZerglingSpeed)
    output &&= ! enemyHydralisksLikely
    output &&= ! enemyMutalisksLikely
    output &&= ! enemyLurkersLikely
    output &&= (_previouslyAnticipatedSpeedlings || units(Protoss.Gateway, Protoss.CyberneticsCore, Protoss.CitadelOfAdun, Protoss.RoboticsFacility, Protoss.Stargate) < 3) // At some point, stop reacting to speedlings
    _previouslyAnticipatedSpeedlings = output
    output
  }

  protected def open(allowExpanding: Boolean): Unit = {
    if (units(Protoss.Gateway) < 2 && units(Protoss.Nexus) < 2 && ! have(Protoss.Assimilator)) {
      if (opening == Open1012 && enemyRecentStrategy(With.fingerprints.fourPool)) {
        opening = Open910
      } else if (With.fingerprints.twelveHatch() && ! With.fingerprints.twoHatchMain() && ! With.fingerprints.twoHatchGas() && allowExpanding && roll("SwapGateNexus", 0.75)) {
        opening = OpenGateNexus
      } else if (With.fingerprints.overpool() || With.fingerprints.twelveHatch()) {
        opening = OpenZZCoreZ
      }
    }
    opening match {
      case Open910      => open910()
      case Open1012     => open1012()
      case OpenZZCoreZ  => openZZCoreZ()
      case _            => openGateNexus()
    }
    if (bases <= 1 && anticipateSpeedlings) {
      get(3, Protoss.Zealot)
      get(Protoss.Forge)
      get(7, Protoss.Zealot)
      get(2, Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireLabelYes(PlaceLabels.DefendEntrance))
    }
    scoutOn(Protoss.Pylon)
    With.blackboard.scoutExpansions.set(false)
    status(anticipateSpeedlings,  "Speedlings")
    status(opening.toString)
  }
}
