package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat}
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPRobo, PvPRobo1012, PvPRobo1Gate, PvPRobo2GateGoon}
import Utilities.GameTime

class PvPRobo extends GameplanImperative {

  var complete: Boolean = false
  var twoGateZealot: Boolean = false
  var twoGateGoon: Boolean = false
  var twoGateReady: Boolean = false
  var zBeforeCore: Boolean = true
  var zAfterCore: Boolean = true
  var fiveZealot: Boolean = false
  var oneGateTech: Boolean = true
  var getObservers: Boolean = true
  var shuttleFirst: Boolean = true
  var shouldAttack: Boolean = false
  var shouldExpand: Boolean = false
  var shouldExpandTriggered: Boolean = false

  override def activated: Boolean = employing(PvPRobo)
  override def completed: Boolean = complete

  override def executeBuild(): Unit = {
    employing(PvPRobo1Gate) // Because we don't explicitly reference it anywhere else
    if (units(Protoss.CyberneticsCore) > 0 && enemyDarkTemplarLikely) {
      buildOrder(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(Protoss.Observer))
    }

    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe))
    if (twoGateZealot) {
      // https://liquipedia.net/starcraft/2_Gate_(vs._Protoss)
      buildOrder(
        Get(2, Protoss.Gateway),
        Get(13, Protoss.Probe),
        Get(Protoss.Zealot),
        Get(2, Protoss.Pylon),
        Get(15, Protoss.Probe),
        Get(3, Protoss.Zealot))
      if (fiveZealot) {
        // https://tl.net/forum/bw-strategy/380852-pvp-2-gate-5-zealot-expand
        buildOrder(
          Get(16, Protoss.Probe),
          Get(3, Protoss.Pylon),
          Get(17, Protoss.Probe),
          Get(5, Protoss.Zealot),
          Get(18, Protoss.Probe))
        if (With.fingerprints.proxyGateway.matches) {
          pump(Protoss.Probe, 12)
          pumpRatio(Protoss.Zealot, 3, 5, Seq(Flat(2.0), Enemy(Protoss.Zealot, 1.0)))
          pump(Protoss.Probe, 18)
        }
        buildOrder(
          Get(4, Protoss.Pylon),
          Get(Protoss.Assimilator),
          Get(19, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(21, Protoss.Probe),
          Get(2, Protoss.Dragoon),
          Get(Protoss.DragoonRange))
      } else {
        // https://tl.net/forum/bw-strategy/567442-pvp-bonyth-style-2-gate-3-zealot-21-gas-guide
        buildOrder(
          Get(Protoss.Assimilator),
          Get(17, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(18, Protoss.Probe),
          Get(3, Protoss.Pylon),
          Get(20, Protoss.Probe),
          Get(2, Protoss.Dragoon),
          Get(Protoss.DragoonRange))
        // This build relies on losing its Zealots to free supply for Dragoons
        // If we've somehow kept the Zealots, insert a Pylon here
        if (units(Protoss.Zealot) > 0) { buildOrder(Get(3, Protoss.Pylon)) }
        buildOrder(
          Get(21, Protoss.Probe),
          Get(6, Protoss.Dragoon))
      }
    } else {
      // https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)
      buildOrder(
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
        if (zAfterCore) {
          buildOrder(Get(2, Protoss.Zealot))
          if (twoGateGoon) {
            buildOrder(
              Get(2, Protoss.Gateway),
              Get(17, Protoss.Probe),
              Get(Protoss.Dragoon),
              Get(18, Protoss.Probe),
              Get(3, Protoss.Pylon),
              Get(20, Protoss.Probe),
              Get(3, Protoss.Dragoon),
              Get(Protoss.DragoonRange),
              Get(4, Protoss.Pylon),
              Get(21, Protoss.Probe))
          }
        }
        buildOrder(Get(17, Protoss.Probe))
      } else {
        buildOrder(
          Get(Protoss.CyberneticsCore),
          Get(14, Protoss.Probe))
        if (zAfterCore) {
          buildOrder(
            Get(Protoss.Zealot),
            Get(2, Protoss.Pylon),
            Get(17, Protoss.Probe),
            Get(Protoss.Dragoon),
            Get(18, Protoss.Probe))
        } else {
          buildOrder(
            Get(15, Protoss.Probe),
            Get(2, Protoss.Pylon),
            Get(17, Protoss.Probe),
            Get(Protoss.Dragoon))
        }
      }
    }
  }

  def execute(): Unit = {
    complete ||= bases > 1
    if (units(Protoss.Gateway) > 1 || units(Protoss.Assimilator) == 0) {
      // TODO: Also do vs. 9-9 gate
      twoGateZealot ||= employing(PvPRobo1012)
      twoGateZealot ||= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon)
    }
    if (twoGateZealot) {
      // TODO: Do vs. 9-9 gate only
      fiveZealot == enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
    }
    twoGateGoon = employing(PvPRobo2GateGoon)
    twoGateGoon &&= ! twoGateZealot
    if (units(Protoss.CyberneticsCore) == 0) {
      zBeforeCore = With.geography.startLocations.size < 3
      zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zBeforeCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
      zBeforeCore ||= twoGateGoon
      zBeforeCore &&= ! twoGateZealot
    }
    if (unitsComplete(Protoss.CyberneticsCore) == 0) {
      zAfterCore = zBeforeCore
      zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
      zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
      zAfterCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
      zAfterCore ||= twoGateGoon
      zAfterCore &&= ! twoGateZealot
    }
    if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility) == 0) {
      oneGateTech = With.strategy.isRamped && enemyBases < 2 && ! enemyStrategy(
        With.fingerprints.nexusFirst,
        With.fingerprints.gatewayFe,
        With.fingerprints.forgeFe,
        With.fingerprints.twoGate,
        With.fingerprints.proxyGateway,
        With.fingerprints.gasSteal)
      oneGateTech &&= ! twoGateZealot
      oneGateTech &&= ! twoGateGoon
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
      shuttleFirst = (With.strategy.isRamped || ! getObservers) && ! enemyDarkTemplarLikely
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
    shouldAttack ||= With.fingerprints.cannonRush.matches
    // Attack when using aggressive builds
    shouldAttack ||= (twoGateZealot || twoGateGoon) && safeToMoveOut
    // Counterattack vs. flimsy builds
    shouldAttack ||= (
      (enemyLowUnitStrategy || enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate))
      && unitsComplete(Protoss.Dragoon) > 0
      && (upgradeComplete(Protoss.DragoonRange) || ! enemyHasUpgrade(Protoss.DragoonRange) || safeToMoveOut))
    // Require DT backstab protection before attacking through a DT
    shouldAttack &&= (unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Protoss.DarkTemplar))
    // Push out to take our natural
    shouldAttack ||= shouldExpand

    status("PvPRobo")
    if (twoGateZealot) {
      status("2Gate")
    } else if (zBeforeCore){
      (if (zAfterCore) status("ZCoreZ") else status("ZCore"))
    } else {
      (if (zAfterCore) status("CoreZ") else status("NZCore"))
    }
    if (twoGateGoon) status("2GateGoon")
    if (fiveZealot) status("5Zealot") else if (twoGateZealot) status("3Zealot")
    if (oneGateTech) status("1GateTech") else status("2GateTech")
    if (getObservers) status("Obs") else status("NoObs")
    if (shuttleFirst) status("ShuttleFirst") else status("ShuttleLater")
    if (shouldAttack) status("Attack") else status("Defend")
    if (shouldExpand) status("ExpandNow") else status("ExpandLater")

    new ScoutForCannonRush().update()
    if (shouldAttack) { attack() }
    if (enemies(Protoss.Dragoon) == 0) {
      if (twoGateZealot) {
        if ( ! foundEnemyBase) scoutOn(Protoss.Gateway, quantity = 2)
      } else if (starts > 3) {
        scoutOn(Protoss.Gateway)
      } else if ( ! zBeforeCore) {
        scoutOn(Protoss.CyberneticsCore)
      }
    }
    if (shouldExpand && With.geography.ourNatural.units.exists(u => u.isEnemy && u.canAttackGround)) { aggression(1.5) }
    else if (twoGateZealot) {
      if (enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)) {
        With.blackboard.pushKiters.set(false)
        With.units.ours.foreach(_.agent.commit = false)
      } else if (frame < GameTime(4, 15)() && enemiesComplete(Protoss.PhotonCannon) == 0) {
        // Wait until we have at least three Zealots together; then go in hard
        aggression(0.75)
        val zealots = With.units.ours.filter(u => Protoss.Zealot(u) && u.battle.exists(_.us.units.count(Protoss.Zealot) > 2)).toVector
        twoGateReady ||= zealots.size > 2
        if (twoGateReady) {
          With.blackboard.pushKiters.set(true)
          zealots.foreach(_.agent.commit = true)
        }

      }
    }
    else if (With.strategy.isInverted) { aggression(1.2) }
    gasLimitCeiling(350)

    if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
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
    get(2, Protoss.Gateway)
    pumpWorkers(oversaturate = true)
    get(3, Protoss.Gateway)
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
    if (units(Protoss.Gateway) >= 3 || enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate) || gas < 42) {
      pump(Protoss.Zealot)
    }
  }

}