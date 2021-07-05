package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

class PvPRoboNew extends GameplanImperative {

  var complete: Boolean = false
  var zBeforeCore: Boolean = true
  var zAfterCore: Boolean = true
  var oneGateTech: Boolean = true
  var getObservers: Boolean = true
  var shuttleFirst: Boolean = true
  var shouldAttack: Boolean = false
  var shouldExpand: Boolean = false
  var shouldExpandTriggered: Boolean = false

  override def activated: Boolean = employing(PvPRobo)
  override def completed: Boolean = complete

  override def executeBuild(): Unit = {
    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe))
    if (zBeforeCore) {
      buildOrder(
        Get(Protoss.Zealot),
        Get(14, Protoss.Probe),
        Get(2, Protoss.Pylon),
        Get(15, Protoss.Probe),
        Get(Protoss.CyberneticsCore),
        Get(16, Protoss.Probe))
      if (zAfterCore) { buildOrder(Get(2, Protoss.Zealot)) }
      buildOrder(Get(17, Protoss.Probe))
    } else {
      buildOrder(
        Get(Protoss.CyberneticsCore),
        Get(14, Protoss.Probe))
      if (zAfterCore) {
        buildOrder(
          Get(Protoss.Zealot),
          Get(2, Protoss.Pylon),
          Get(16, Protoss.Probe))
      } else {
        buildOrder(
          Get(15, Protoss.Probe),
          Get(2, Protoss.Pylon),
          Get(17, Protoss.Probe),
          Get(Protoss.Dragoon))
      }
    }
  }

  def execute(): Unit = {
    complete ||= bases > 1
    if (units(Protoss.CyberneticsCore) == 0) {
      zBeforeCore = With.geography.startLocations.size < 3
      zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zBeforeCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
    }
    if (unitsComplete(Protoss.CyberneticsCore) == 0) {
      zAfterCore = zBeforeCore
      zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
      zAfterCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
    }
    if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility) == 0) {
      oneGateTech = With.strategy.isRamped && enemyBases < 2 && ! enemyStrategy(
        With.fingerprints.nexusFirst,
        With.fingerprints.gatewayFe,
        With.fingerprints.forgeFe,
        With.fingerprints.twoGate,
        With.fingerprints.proxyGateway,
        With.fingerprints.gasSteal)
    }
    getObservers = enemyDarkTemplarLikely
    if (units(Protoss.RoboticsSupportBay) == 0) {
      getObservers ||= oneGateTech && ! enemyStrategy(
      With.fingerprints.nexusFirst,
      With.fingerprints.proxyGateway,
      With.fingerprints.dragoonRange,
      With.fingerprints.cannonRush,
      With.fingerprints.robo,
      With.fingerprints.threeGateGoon,
      With.fingerprints.fourGateGoon)
      shuttleFirst = With.strategy.isRamped || ! getObservers
    }
    shouldExpand = units(Protoss.Gateway) >= 2
    shouldExpand &&= ! With.fingerprints.dtRush.matches || unitsComplete(Protoss.Observer) > 0
    shouldExpand &&= ! With.fingerprints.dtRush.matches || (units(Protoss.Observer) > 0 && enemies(Protoss.DarkTemplar) == 0)
    shouldExpand &&= shouldExpandTriggered || (
      (safeToMoveOut && enemyStrategy(With.fingerprints.dtRush, With.fingerprints.twoGate))
      || (safeToMoveOut && enemyLowUnitStrategy && unitsComplete(Protoss.Reaver) > 0)
      || unitsComplete(Protoss.Reaver, Protoss.Shuttle) >= 2)
    shouldExpandTriggered ||= shouldExpand
    shouldAttack = unitsComplete(Protoss.Zealot) > 0 && enemiesComplete(MatchWarriors, Protoss.PhotonCannon) == 0
    shouldAttack ||= shouldExpand
    shouldAttack ||= With.fingerprints.cannonRush.matches
    // Counterattack vs. flimsy builds
    shouldAttack ||= (
      (enemyLowUnitStrategy || enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate))
      && unitsComplete(Protoss.Dragoon) > 0
      && (upgradeComplete(Protoss.DragoonRange) || ! enemyHasUpgrade(Protoss.DragoonRange) || safeToMoveOut))
    shouldAttack &&= ( ! With.fingerprints.dtRush.matches || unitsComplete(Protoss.Observer) > 1)

    if (zBeforeCore)  { (if (zAfterCore) status("ZCoreZ") else status("ZCore")) }
    else              { (if (zAfterCore) status("CoreZ") else status("NZCore")) }
    if (getObservers) status("Obs") else status("NoObs")
    if (shuttleFirst) status("ShuttleFirst") else status("ShuttleLater")
    if (shouldAttack) status("Attack") else status("Defend")
    if (shouldExpand) status("ExpandNow") else status("ExpandLater")

    new ScoutForCannonRush().update()
    if (shouldAttack) { attack() }
    if (enemies(Protoss.Dragoon) == 0) { if (starts > 3) scoutOn(Protoss.Gateway) else scoutOn(Protoss.CyberneticsCore) }
    if (shouldExpand) { aggression(3.0) }
    else if (With.strategy.isInverted) {  aggression(1.5) }
    else if (With.strategy.isFlat) {  aggression(1.2) }
    gasLimitCeiling(300)

    if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
      gasWorkerCeiling(1)
    } else if ( ! oneGateTech && units(Protoss.Pylon) < 3) {
      gasWorkerCeiling(2)
    }
    if (oneGateTech) {
      // TODO: Polish based on XCoreX
      buildOrder(
        Get(Protoss.Dragoon),
        Get(Protoss.DragoonRange),
        Get(3, Protoss.Pylon))
    } else {
      // TODO: Polish based on XCoreX
      buildOrder(
        Get(2, Protoss.Gateway),
        Get(Protoss.Dragoon),
        Get(Protoss.DragoonRange),
        Get(3, Protoss.Pylon),
        Get(3, Protoss.Dragoon))
    }
    get(Protoss.RoboticsFacility)
    buildOrder(Get(2, Protoss.Dragoon))

    trainRoboUnits()

    if (getObservers) {
      if (enemyDarkTemplarLikely) {
        if (units(Protoss.Observatory) == 0) { cancelIncomplete(Protoss.RoboticsSupportBay) }
        if (units(Protoss.Observer) == 0) { cancelIncomplete(Protoss.Shuttle, Protoss.Reaver) }
        cancelIncomplete()
      }
      get(Protoss.Observatory)
      if (units(Protoss.Observer) > 0) { get(Protoss.RoboticsSupportBay) }
    } else {
      cancelIncomplete(Protoss.Observatory)
      cancelIncomplete(Protoss.Observer)
      get(Protoss.RoboticsSupportBay)
    }

    if (shouldExpand && ! With.geography.ourNatural.units.exists(u => u.isEnemy && u.canAttack)) { requireMiningBases(2) }

    trainGatewayUnits()

    if (With.fingerprints.dtRush.matches) { get(Protoss.ObserverSpeed) }
    pumpWorkers(oversaturate = true)
    if (minerals >= 300) { get(3, Protoss.Gateway) }
  }

  private def enemyLowUnitStrategy: Boolean = enemyBases > 1 || enemyStrategy(
    With.fingerprints.nexusFirst,
    With.fingerprints.gatewayFe,
    With.fingerprints.forgeFe,
    With.fingerprints.robo,
    With.fingerprints.dtRush,
    With.fingerprints.cannonRush)

  private def trainRoboUnits(): Unit = {
    if (units(Protoss.RoboticsFacility) > 0) {
      if (getObservers) {
        buildOrder(Get(Protoss.Observer))
        if (With.fingerprints.dtRush.matches) pump(Protoss.Observer, 2)
      }
      if (shuttleFirst) buildOrder(Get(Protoss.Shuttle))
      if (units(Protoss.Reaver) >= (if (enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) 3 else 2)) pumpShuttleAndReavers() else pump(Protoss.Reaver)
    }
  }

  private def trainGatewayUnits(): Unit = {
    if (zAfterCore && zBeforeCore) buildOrder(Get(2, Protoss.Zealot))
    else if (zAfterCore || zBeforeCore) buildOrder(Get(Protoss.Zealot))
    buildOrder(Get(Protoss.Dragoon))
    pump(Protoss.Dragoon)
    if (units(Protoss.Gateway) >= 2) pump(Protoss.Zealot)
  }

}