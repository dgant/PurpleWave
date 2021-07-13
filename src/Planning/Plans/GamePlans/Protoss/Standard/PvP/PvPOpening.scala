package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Placement.BuildCannonsAtNatural
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Utilities.GameTime

class PvPOpening extends GameplanImperative {

  var complete: Boolean = false
  var commitZealots: Boolean = false
  var zBeforeCore: Boolean = false
  var zAfterCore: Boolean = false
  var sevenZealot: Boolean = false
  var shouldExpand: Boolean = false
  var shouldAttack: Boolean = false
  // Robo properties
  var getObservers: Boolean = false
  var getObservatory: Boolean = false
  var shuttleFirst: Boolean = false
  // DT properties
  var getCannons: Boolean = false

  override def activated: Boolean = employing(PvPRobo, PvPDT, PvP3GateGoon, PvP4GateGoon)
  override def completed: Boolean = { complete ||= bases > 1; complete }

  val buildCannonsAtNatural = new BuildCannonsAtNatural(2)
  val reactToDTEmergencies = new PvPIdeas.ReactToDarkTemplarEmergencies
  override def executeBuild(): Unit = {

    /////////////////////
    // Update strategy //
    /////////////////////

    if (units(Protoss.Assimilator) == 0 || units(Protoss.Gateway) < 2) {
      // TODO: Also react vs. 9-9 gate
      var twoGate = employing(PvP1012)
      twoGate ||= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon)
      twoGate &&= ! enemyStrategy(With.fingerprints.cannonRush)
      if (twoGate) {
        PvP1012.swapIn()
        PvPGateCoreTech.swapOut()
        PvPGateCoreGate.swapOut()
      } else {
        PvP1012.swapOut()
        // Revert to the original Core strategy if possible
        (With.strategy.selected.toVector.view ++ With.strategy.selectedInitially)
          .find(x => x == PvPGateCoreTech || x == PvPGateCoreGate)
          .getOrElse(PvPGateCoreTech)
          .swapIn()
      }
    }
    if (employing(PvP1012)) {
      if (units(Protoss.Assimilator) == 0) {
        // TODO: Against 10-12 it's okay to stay 3-Zealot. We only need 5-Zealot vs 9-9.
        var fiveZealot = employing(PvP5Zealot)
        fiveZealot ||= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst, With.fingerprints.gasSteal)
        if (fiveZealot) {
          PvP5Zealot.swapIn()
          PvP3Zealot.swapOut()
        } else {
          PvP3Zealot.swapIn()
          PvP5Zealot.swapOut()
        }
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        sevenZealot = employing(PvP5Zealot)
        sevenZealot &&= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
      }
    } else if (employing(PvPGateCoreTech, PvPGateCoreGate)) {
      if (units(Protoss.CyberneticsCore) == 0) {
        zBeforeCore = With.geography.startLocations.size < 3
        zBeforeCore &&= ! employing(PvPDT)
        zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zBeforeCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zBeforeCore ||= employing(PvPGateCoreGate, PvP3GateGoon, PvP4GateGoon)
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        zAfterCore = zBeforeCore
        zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zAfterCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
        zAfterCore ||= employing(PvPGateCoreGate)
      }
      if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility) < 1 && units(Protoss.CitadelOfAdun) < 1) {
        var gateCoreGate = employing(PvPGateCoreGate)
        gateCoreGate ||= enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst)
        if (gateCoreGate) {
          PvPGateCoreGate.swapIn()
          PvPGateCoreTech.swapOut()
        } else {
          PvPGateCoreTech.swapIn()
          PvPGateCoreGate.swapOut()
        }
      }
    }

    /////////////////
    // Tech switch //
    /////////////////

    // If we catch them going Robo against our DT, go goon-only
    if (employing(PvPDT) && (enemyRobo || enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe))) {
      PvPDT.swapOut()
      (if (roll("Switch4Gate", 0.6)) PvP4GateGoon else PvP3GateGoon).swapIn()
    }

    /////////////////////////////
    // Tech-specific decisions //
    /////////////////////////////

    if (employing(PvPRobo)) {
      getObservatory = true
      getObservers = true
      if (enemyDarkTemplarLikely || enemies(Protoss.CitadelOfAdun) > 0) {
        shuttleFirst = false
      } else {
        if (units(Protoss.Shuttle) == 0) {
          if (units(Protoss.RoboticsSupportBay, Protoss.Observer) == 0) {
            shuttleFirst = true
          }
        }

        // Look for reasons to avoid making an Observer.
        // Don't stop to check if we already started an Observatory or Observers
        // because we can cancel and switch out of them at any time.

        getObservatory = true
        getObservers = true

        if (enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
          // These builds generally let us rule out DT entirely
          getObservatory = false
          getObservers = false
        } else if (trackRecordLacks(With.fingerprints.dtRush) || enemyStrategy(With.fingerprints.dragoonRange, With.fingerprints.twoGate, With.fingerprints.proxyGateway)) {
          if (enemyRecentStrategy(With.fingerprints.fourGateGoon, With.fingerprints.threeGateGoon) && ! enemyRecentStrategy(With.fingerprints.dtRush)) {
            getObservatory = false
            getObservers = false
          } else {
            getObservatory = roll("SpeculativeObservatory", 0.5)
            getObservers = getObservatory && roll("SpeculativeObservers", 0.5) // So the probability of obs is the *joint* probability
          }
        }
      }

      shouldExpand = units(Protoss.Gateway) >= 2
      shouldExpand &&= (
            ((shouldExpand || safeToMoveOut) && enemyStrategy(With.fingerprints.dtRush) && unitsComplete(Protoss.Observer) > 0)
        ||  ((shouldExpand || safeToMoveOut) && enemyLowUnitStrategy && unitsComplete(Protoss.Reaver) > 0)
        || unitsComplete(Protoss.Reaver) >= 2)
    } else if (employing(PvPDT)) {
      // Look for reasons to avoid getting cannons
      if (enemyDarkTemplarLikely) {
        getCannons = true
      } else {
        getCannons = units(Protoss.TemplarArchives) > 0
        getCannons &&= safeAtHome
        getCannons &&= ! enemyRobo
        getCannons &&= enemyBases < 2
        getCannons &&= ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
        getCannons &&= roll("DTSkipCannons", 0.5)
      }
      shouldExpand ||= unitsComplete(Protoss.DarkTemplar) > 0 || (safeToMoveOut && units(Protoss.DarkTemplar) > 0)
    } else if (employing(PvP3GateGoon)) {
      shouldExpand = unitsComplete(Protoss.Gateway) >= 3 && unitsComplete(MatchWarriors) >= 6
    } else if (employing(PvP4GateGoon)) {
      shouldExpand = unitsComplete(MatchWarriors) >= (if (safeToMoveOut) 20 else 28)
    }
    shouldExpand &&= ! With.fingerprints.dtRush.matches || unitsComplete(Protoss.Observer, Protoss.PhotonCannon) > 0
    shouldExpand &&= ! With.fingerprints.dtRush.matches || (units(Protoss.Observer, Protoss.PhotonCannon) > 0 && enemies(Protoss.DarkTemplar) == 0)

    shouldAttack = unitsComplete(Protoss.Zealot) > 0 && enemiesComplete(MatchWarriors, Protoss.PhotonCannon) == 0
    shouldAttack ||= With.fingerprints.cannonRush.matches
    // Attack when using a more aggressive build
    shouldAttack ||= safeToMoveOut &&
      (enemyLowUnitStrategy
        || (employing(PvP1012) && (unitsComplete(Protoss.Zealot) > 3 || ! enemyStrategy(With.fingerprints.twoGate))
        || (employing(PvPGateCoreGate) && unitsComplete(Protoss.Dragoon) > 0)))
    // Attack when we have range advantage
    shouldAttack ||= unitsComplete(Protoss.Dragoon) > 0     && ! enemyHasShown(Protoss.Dragoon)         && (enemiesShown(Protoss.Zealot) > 2 || With.fingerprints.twoGate.matches)
    shouldAttack ||= upgradeComplete(Protoss.DragoonRange)  && ! enemyHasUpgrade(Protoss.DragoonRange)  && (enemiesShown(Protoss.Zealot) > 2 || With.fingerprints.twoGate.matches)
    // Require DT backstab protection before attacking through a DT
    shouldAttack &&= (unitsComplete(Protoss.Observer) > 1 || ! enemyHasShown(Protoss.DarkTemplar))
    // Push out to take our natural
    shouldAttack ||= shouldExpand
    // Ensure that committed Zealots keep wanting to attack
    shouldAttack ||= With.units.ours.exists(u => u.agent.commit)

    /////////////
    // Logging //
    /////////////

    if (employing(PvPGateCoreTech, PvPGateCoreGate)) {
      if (zBeforeCore) {
        (if (zAfterCore) status("ZCoreZ") else status("ZCore"))
      } else {
        (if (zAfterCore) status("CoreZ") else status("NZCore"))
      }
    }

    if (getObservers) status("Obs")
    if (getObservatory) status("Observatory")
    if (shuttleFirst) status("ShuttleFirst")
    if (getCannons) status("Cannons")
    if (commitZealots) status("CommitZealots")
    if (shouldAttack) status("Attack")
    if (shouldExpand) status("ExpandNow")
    oversaturate = true
    if (shouldAttack) { attack() }

    ////////////////////////////
    // Emergency DT reactions //
    ////////////////////////////

    if (units(Protoss.CyberneticsCore) > 0 && enemyDarkTemplarLikely) {
      if (employing(PvPRobo)) {
        buildOrder(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer))
      } else {
        reactToDTEmergencies.update()
      }
    }

    //////////////
    // Scouting //
    //////////////

    if (enemies(Protoss.Dragoon) == 0 && ! enemyStrategy(With.fingerprints.proxyGateway)) {
      if (employing(PvP1012)) {
        if ( ! foundEnemyBase) {
          scoutOn(Protoss.Gateway, quantity = 2)
        }
      } else if (starts > 3) {
        scoutOn(Protoss.Gateway)
      } else {
        scoutOn(Protoss.CyberneticsCore)
      }
    }

    /////////////////
    // Zealot rush //
    /////////////////

    if (employing(PvP1012)) {
      if (enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)) {
        With.blackboard.pushKiters.set(false)
        With.units.ours.foreach(_.agent.commit = false)
      } else if (frame < GameTime(4, 15)() && enemiesComplete(Protoss.PhotonCannon) == 0) {
        // Wait until we have at least three Zealots together; then go in hard
        aggression(0.75)
        val zealots = With.units.ours.filter(u => Protoss.Zealot(u) && u.battle.exists(_.us.units.count(Protoss.Zealot) > 2)).toVector
        commitZealots ||= zealots.size > 2
        if (commitZealots) {
          With.blackboard.pushKiters.set(true)
          zealots.foreach(_.agent.commit = true)
        }
      }
    }

    /////////////////////////
    // Execute build order //
    /////////////////////////

    buildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway))

    ////////////
    // 2-Gate //
    ////////////

    if (employing(PvP1012)) { // https://liquipedia.net/starcraft/2_Gate_(vs._Protoss)
      buildOrder(
        Get(12, Protoss.Probe),
        Get(2, Protoss.Gateway),
        Get(13, Protoss.Probe),
        Get(Protoss.Zealot),
        Get(2, Protoss.Pylon),
        Get(15, Protoss.Probe),
        Get(3, Protoss.Zealot))

      ///////////////
      // 5+ Zealot //
      ///////////////

      if (employing(PvP5Zealot)) { // https://tl.net/forum/bw-strategy/380852-pvp-2-gate-5-zealot-expand
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
          Get(Protoss.CyberneticsCore))
        if (sevenZealot) {
          buildOrder(Get(7, Protoss.Zealot))
        } else {
          buildOrder(Get(3, Protoss.Gateway))
        }
        buildOrder(
          Get(21, Protoss.Probe),
          Get(3, Protoss.Gateway),
          Get(2, Protoss.Dragoon),
          Get(Protoss.DragoonRange))

      //////////////
      // 3-Zealot //
      //////////////

      } else { // https://tl.net/forum/bw-strategy/567442-pvp-bonyth-style-2-gate-3-zealot-21-gas-guide
        buildOrder(
          Get(Protoss.Assimilator),
          Get(17, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(18, Protoss.Probe),
          Get(3, Protoss.Pylon),
          Get(20, Protoss.Probe),
          Get(4, Protoss.Pylon), // On paper this build requires losing the Zealots to free supply, but with mineral optimization we can easily afford the Pylon
          Get(2, Protoss.Dragoon),
          Get(21, Protoss.Probe),
          Get(Protoss.DragoonRange),
          Get(22, Protoss.Probe),
          Get(3, Protoss.Gateway), // Also not in the build but we can afford it so let's
          Get(4, Protoss.Dragoon),
          Get(23, Protoss.Probe),
          Get(5, Protoss.Pylon),
          Get(24, Protoss.Probe),
          Get(6, Protoss.Dragoon))
      }

    /////////////////
    // 1 Gate Core //
    /////////////////

    } else {
      buildOrder(
        Get(12, Protoss.Probe),
        Get(Protoss.Assimilator),
        Get(13, Protoss.Probe))

      /////////////
      // 17 Core //
      /////////////

      if (zBeforeCore) { // https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)
        buildOrder(
          Get(Protoss.Zealot),
          Get(14, Protoss.Probe),
          Get(2, Protoss.Pylon),
          Get(15, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(16, Protoss.Probe))
        if (zAfterCore) {
          buildOrder(Get(2, Protoss.Zealot))
          if (employing(PvPGateCoreGate)) { // https://liquipedia.net/starcraft/2_Gate_Reaver_(vs._Protoss)
            buildOrder(
              Get(18, Protoss.Probe),
              Get(3, Protoss.Pylon),
              Get(19, Protoss.Probe),
              Get(Protoss.Dragoon),
              Get(20, Protoss.Probe),
              Get(2, Protoss.Gateway),
              Get(21, Protoss.Probe),
              Get(3, Protoss.Dragoon),
              Get(3, Protoss.Dragoon),
              Get(Protoss.DragoonRange),
              Get(4, Protoss.Pylon),
              Get(21, Protoss.Probe))
          }
        }
        buildOrder(Get(17, Protoss.Probe))

      /////////////
      // 13 Core //
      /////////////

      } else {
        // https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)
        buildOrder(Get(Protoss.CyberneticsCore))
        if (zAfterCore) {
          // https://namu-wiki.translate.goog/w/21%ED%88%AC%EA%B2%8C%EC%9D%B4%ED%8A%B8?_x_tr_sl=ko&_x_tr_tl=en&_x_tr_hl=en&_x_tr_pto=ajax,sc,elem
          buildOrder(
            Get(14, Protoss.Probe),
            Get(Protoss.Zealot),
            Get(2, Protoss.Pylon),
            Get(16, Protoss.Probe),
            Get(Protoss.Dragoon),
            Get(Protoss.DragoonRange),
            Get(17, Protoss.Probe))
          if (employing(PvPGateCoreGate)) {
            buildOrder(
              Get(2, Protoss.Gateway),
              Get(2, Protoss.Dragoon),
              Get(18, Protoss.Probe),
              Get(3, Protoss.Pylon))
          } else {
            buildOrder(
              Get(2, Protoss.Dragoon),
              Get(18, Protoss.Probe))
          }
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

    gasLimitCeiling(350)
    if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
      gasWorkerCeiling(2)
    }

    ////////////////////////
    // Transition to tech //
    ////////////////////////

    // The build order should have requested all of these, but just in case:
    buildOrder(Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore))
    if (employing(PvPGateCoreTech)) {
      buildOrder(
        Get(Protoss.Dragoon),
        Get(Protoss.DragoonRange),
        Get(3, Protoss.Pylon))
    } else {
      buildOrder(
        Get(2, Protoss.Gateway),
        Get(Protoss.Dragoon),
        Get(Protoss.DragoonRange),
        Get(3, Protoss.Pylon),
        Get(3, Protoss.Dragoon))
    }
    if (employing(PvP1012)) {
      // We're tight on gas and can fit in a round of Zealots
      new BuildOrder(
        Get(5, Protoss.Zealot),
        Get(3, Protoss.Gateway))
    }

    //////////
    // Tech //
    //////////

    if (employing(PvPRobo)) {
      get(Protoss.RoboticsFacility)

      trainRoboUnits()

      if (getObservers) {
        if (enemyDarkTemplarLikely) {
          if (units(Protoss.Observatory) == 0) {
            cancelIncomplete(Protoss.RoboticsSupportBay)
          }
          if (units(Protoss.Observer) == 0) {
            cancelIncomplete(Protoss.Shuttle, Protoss.Reaver)
          }
          cancelIncomplete()
        }
        get(Protoss.Observatory)
        if (units(Protoss.Observer) > 0) {
          get(Protoss.RoboticsSupportBay)
        }
      } else {
        cancelIncomplete(Protoss.Observatory)
        cancelIncomplete(Protoss.Observer)
        get(Protoss.RoboticsSupportBay)
      }

      if (shouldExpand && ! With.geography.ourNatural.units.exists(u => u.isEnemy && u.canAttack)) { requireMiningBases(2) }

      if (With.fingerprints.dtRush.matches) { get(Protoss.ObserverSpeed) }
      trainGatewayUnits()

      get(3, Protoss.Gateway)

    } else if (employing(PvPDT)) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.TemplarArchives)
      buildOrder(Get(Protoss.DarkTemplar))
      // Super-fast DT finishes 5:12 and thus arrives at the natural around 5:45
      // Example: http://www.openbw.com/replay-viewer/?rep=https://data.basil-ladder.net/bots/MegaBot2017/MegaBot2017%20vs%20Florian%20Richoux%20Heartbreak%20Ridge%20CTR_EA637F71.rep
      // Pylon + Forge + Cannon takes 1:15
      // So we need to start the cannon process no later than 4:30 (adding some time as a buffer for construction delays)
      // We can delay this based on things we've seen
      var timeToStartCannons = GameTime(4, 30)()
      // TODO: Delay if they went Zealot-first into core
      if (enemyStrategy(With.fingerprints.twoGate)) timeToStartCannons += GameTime(1, 10)()
      if (enemyStrategy(With.fingerprints.dragoonRange)) timeToStartCannons += GameTime(0, 30)()
      if (getCannons && With.frame > timeToStartCannons) {
        buildCannonsAtNatural.update()
      }
      if (shouldExpand) { requireMiningBases(2) }
      if ( ! enemyRobo) pump(Protoss.DarkTemplar, 1)
      trainGatewayUnits()
      if (units(Protoss.TemplarArchives) > 0) {
        requireMiningBases(2)
      } else {
        get(4, Protoss.Gateway)
      }
    } else if (employing(PvP3GateGoon)) {
      trainGatewayUnits()
      get(3, Protoss.Gateway)
      buildOrder(Get(8, Protoss.Dragoon))
      requireMiningBases(2)
    } else {
      if (shouldExpand) { requireMiningBases(2) }
      trainGatewayUnits()
      get(4, Protoss.Gateway)
    }
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