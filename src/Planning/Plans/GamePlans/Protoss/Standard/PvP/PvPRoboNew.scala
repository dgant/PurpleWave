package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Planning.Plans.GamePlans.GameplanImperative
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

class PvPRoboNew extends GameplanImperative {
  override def doIf: Boolean = employing(PvPRobo)

  var zBeforeCore: Boolean = true
  var zAfterCore: Boolean = true
  var oneGateTech: Boolean = true
  var getObservers: Boolean = true
  var shuttleFirst: Boolean = true
  var shouldExpand: Boolean = false
  var shouldAttack: Boolean = false

  val enemyLowUnitStrategy: Boolean = enemyBases > 1 || enemyStrategy(
    With.fingerprints.robo,
    With.fingerprints.dtRush,
    With.fingerprints.forgeFe)

  override def buildOrder(): Unit = {
    get(8, Protoss.Probe)
    get(Protoss.Pylon)
    get(10, Protoss.Probe)
    get(Protoss.Gateway)
    get(12, Protoss.Probe)
  }

  def execute(): Unit = {
    if (units(Protoss.CyberneticsCore) == 0) {
      zBeforeCore = With.geography.startLocations.size < 3
      zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zBeforeCore ||= enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon)
    }
    if (unitsComplete(Protoss.CyberneticsCore) == 0) {
      zAfterCore = zBeforeCore
      zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zAfterCore ||= enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon)
    }
    if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility) == 0) {
      oneGateTech = With.strategy.isRamped && enemyBases < 2 && ! enemyStrategy(
        With.fingerprints.nexusFirst,
        With.fingerprints.gatewayFe,
        With.fingerprints.forgeFe,
        With.fingerprints.twoGate,
        With.fingerprints.proxyGateway)
    }
    getObservers = enemyDarkTemplarLikely
    if (units(Protoss.RoboticsSupportBay) == 0) {
      getObservers ||= oneGateTech && ! enemyStrategy(
      With.fingerprints.nexusFirst,
      With.fingerprints.dragoonRange,
      With.fingerprints.robo,
      With.fingerprints.threeGateGoon,
      With.fingerprints.fourGateGoon)
      shuttleFirst = With.strategy.isRamped || ! getObservers
    }
  }
  shouldExpand = units(Protoss.Gateway) >= 2
  shouldExpand &&= ! With.fingerprints.dtRush.matches || unitsComplete(Protoss.Observer) > 0
  shouldExpand &&= ! With.fingerprints.dtRush.matches || (units(Protoss.Observer) > 0 && enemies(Protoss.DarkTemplar) == 0)
  shouldExpand &&= (
    (safeToMoveOut && enemyStrategy(With.fingerprints.dtRush, With.fingerprints.twoGate))
    || (safeToMoveOut && enemyLowUnitStrategy && unitsComplete(Protoss.Reaver) > 0)
    || unitsComplete(Protoss.Reaver, Protoss.Shuttle) >= 2)
  shouldAttack = enemiesComplete(MatchWarriors, Protoss.PhotonCannon) == 0
  shouldAttack ||= shouldExpand

  if (zBeforeCore)  (if (zAfterCore) status("ZCoreZ") else status("ZCore"))
  else              (if (zAfterCore) status("CoreZ") else status("NZCore"))
  if (getObservers) status("Obs") else status("NoObs")
  if (shuttleFirst) status("ShuttleFirst") else status("ShuttleLater")
  if (shouldExpand) status("Expand") else status("NoExpand")
  if (shouldAttack) status("Attack") else status("Defend")
}