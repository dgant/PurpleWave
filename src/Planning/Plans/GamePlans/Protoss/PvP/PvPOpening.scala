package Planning.Plans.GamePlans.Protoss.PvP

import Lifecycle.With
import Macro.Requests.Get
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Strategery._
import Utilities.SwapIf
import Utilities.Time.{Frames, GameTime, Minutes, Seconds}
import Utilities.UnitFilters.{IsAll, IsComplete, IsWarrior}

class PvPOpening extends GameplanImperative {

  var complete: Boolean = false
  // General properties
  var shouldExpand: Boolean = false
  var shouldAttack: Boolean = false
  var shouldHarass: Boolean = false
  // 10-12 properties
  var commitZealots: Boolean = false
  var sevenZealot: Boolean = false
  // 1 Gate Core properties
  var zBeforeCore: Boolean = false
  var zAfterCore: Boolean = false
  // Robo properties
  var getObservers: Boolean = false
  var getObservatory: Boolean = false
  var shuttleFirst: Boolean = false
  // DT properties
  var timeToStartCannons: Int = 0
  var getCannons: Boolean = false
  var speedlotAttack: Boolean = false

  override def activated: Boolean = true
  override def completed: Boolean = { complete ||= bases > 1; complete }

  override def executeBuild(): Unit = {

    /////////////////////
    // Update strategy //
    /////////////////////

    // Swap into 2-Gate
    if (units(Protoss.Assimilator) == 0) {
      if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon) && ! With.fingerprints.cannonRush()) {
        PvP1012.swapIn()
        PvPGateCoreTech.swapOut()
        PvPGateCoreGate.swapOut()
        PvPTechBeforeRange.swapOut()
      }
    }
    if (PvP1012()) {
      if (units(Protoss.Assimilator) == 0) {
        PvP3Zealot()
        PvP5Zealot()
        // TODO: Against 10-12 it's okay to stay 3-Zealot. We only need 5-Zealot vs 9-9.
        if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst, With.fingerprints.gasSteal)) {
          PvP5Zealot.swapIn()
          PvP3Zealot.swapOut()
        }
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        sevenZealot = PvP5Zealot()
        sevenZealot &&= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
      }
    } else {
      if (units(Protoss.CyberneticsCore) == 0) {
        zBeforeCore = With.geography.startLocations.size < 3
        zBeforeCore &&= ! PvPDT()
        zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zBeforeCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zBeforeCore ||= PvPGateCoreGate() || PvP3GateGoon() || PvP4GateGoon()
        zBeforeCore &&= ! PvPTechBeforeRange()
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        zAfterCore = zBeforeCore
        zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zAfterCore ||= With.fingerprints.mannerPylon()
        zAfterCore ||= With.fingerprints.gasSteal()
        zAfterCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
        zAfterCore ||= PvPGateCoreGate() || PvPTechBeforeRange() || PvPDT()
      }
      if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility, Protoss.CitadelOfAdun) < 1) {
        if (With.fingerprints.twoGate() || With.fingerprints.proxyGateway() || With.fingerprints.nexusFirst()) {
          PvPGateCoreGate.swapIn()
          PvPGateCoreTech.swapOut()
          PvPTechBeforeRange.swapOut()
        } else if (With.fingerprints.cannonRush() || (With.fingerprints.earlyForge() && With.fingerprints.cannonRush.recently)) {
          PvPGateCoreTech.swapIn()
          PvPTechBeforeRange.swapIn()
          PvPRobo.swapIn()
          PvP1012.swapOut()
          PvPDT.swapOut()
          PvP3GateGoon.swapOut()
          PvP4GateGoon.swapOut()
        } else if (PvPGateCoreGate() && With.strategy.isRamped) {
          PvPGateCoreGate.swapOut()
          PvPGateCoreTech.swapIn()
        }
      }
    }

    /////////////////
    // Tech switch //
    /////////////////

    // Randomly switch the learning-ordained tech based on intel and opponent tendencies,
    // to augment learning and force the opponent to play a diverse set of strategies.
    //
    // https://tl.net/forum/bw-strategy/526298-pvp-common-builds-and-what-counters-it-t-l
    // has some good details on the metagame rock-paper-scissors.

    // These maps are too long for 2-Gate unless we're failing to hold proxies otherwise
    if (PvP1012()
      && units(Protoss.Gateway) == 0
      && ! enemyRecentStrategy(With.fingerprints.proxyGateway) && Seq(Arcadia, Aztec, Benzene, Longinus, MatchPoint, Heartbreak, Roadkill).exists(_())) {
      PvP1012.swapOut()
      PvP3Zealot.swapOut()
      PvP5Zealot.swapOut()
      (if (Seq(Heartbreak, Roadkill).exists(_()) || roll("1012ToGateCoreGate", 0.35)) PvPGateCoreGate else PvPGateCoreTech).swapIn()
    }
    // If we catch them going Robo against our DT, go goon-only
    if (PvPDT() && (enemyRobo || enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe))) {
      PvPDT.swapOut()
      (if (roll("SwapDTInto4Gate", 0.5)) PvP4GateGoon else PvP3GateGoon).swapIn()
      cancelIncomplete(Protoss.CitadelOfAdun)
      cancelIncomplete(Protoss.TemplarArchives)
      if (enemies(Protoss.Observer) > 0 || enemies(Protoss.Observatory) > 0) {
        cancelIncomplete(Protoss.DarkTemplar)
      }
    }
    // Goon+Obs is the strongest punishment against badly hidden DT openers.
    // A glimpse of Citadel doesn't sufficiently justify switching into Obs for its own sake,
    // as the Citadel could be a fake and the investment is a lot less than making even one Observer,
    // but a switch into full-blown Robotics at least lets us benefit from the investment if the Citadel was a fake
    if (employing(PvP3GateGoon, PvP4GateGoon) && enemies(Protoss.CitadelOfAdun) > 0 && units(Protoss.Gateway) < 3) {
      if (roll("Swap34GateIntoRoboVsCitadel", 0.3)) {
        PvP3GateGoon.swapOut()
        PvP4GateGoon.swapOut()
        PvPRobo.swapIn()
      }
    }
    // Robo is a very middle-of-the-road build, and has a few pointed weaknesses.
    // It's good against opponents playing diverse strategies but unimpressive against one-dimensional opponents.
    if (PvPRobo()
      && upgradeStarted(Protoss.DragoonRange)
      && units(Protoss.RoboticsFacility) == 0
      && enemies(Protoss.CitadelOfAdun) == 0
      && trackRecordLacks(With.fingerprints.dtRush)) {

      // 4-Gating quickly becomes a lot less appealing with more DT in the mix.
      if (PvPRobo()
        && ! With.strategy.isFixedOpponent
        && trackRecordLacks(With.fingerprints.robo)) {
        if (roll("SwapRoboIntoDT", if (enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) 0.6 else 0.3)) {
          PvPRobo.swapOut()
          PvPDT.swapIn()
        }
      }
      // 3/4-Gate Goon are advantaged against most Robo variants.
      // But we don't want to make this switch too predictably, as it's abusable.
      if (PvPRobo()
        && ! With.strategy.isFixedOpponent
        && enemyRecentStrategy(With.fingerprints.robo) ) {
        if (roll("SwapRoboInto3Gate", if (With.fingerprints.robo()) 0.35 else 0.2)) {
          PvPRobo.swapOut()
          PvP3GateGoon.swapIn()
        } else if (roll("SwapRoboInto4Gate", if (With.fingerprints.robo()) 0.35 else 0.2)) {
          PvPRobo.swapOut()
          PvP4GateGoon.swapIn()
        }
      }
    }
    // Oops. We let them scout our DT rush. Maybe we can use it to our advantage.
    if (PvPDT()
      && scoutCleared
      && With.units.ours.filter(Protoss.TemplarArchives).exists(a =>
        a.hasEverBeenVisibleToOpponents
        && ! a.visibleToOpponents
        && ! a.zone.units.exists(_.isEnemy))) {
      if ( ! enemyRecentStrategy(With.fingerprints.dtRush) && roll("DTTo3Gate", 0.5)) {
        cancelIncomplete(Protoss.TemplarArchives)
        PvPDT.swapOut()
        PvP3GateGoon.swapIn()
      }
      if (roll("DTToSpeedlot", 0.3)) {
        cancelIncomplete(Protoss.TemplarArchives)
        speedlotAttack = true
      }
    }
    // Robo + tech before range die to DTs due to lack of gas for Observers
    if (PvPRobo() && PvPTechBeforeRange() && enemyRecentStrategy(With.fingerprints.dtRush)) {
      PvPTechBeforeRange.swapOut()
      PvPGateCoreTech.swapIn()
    }

    /////////////////////////////
    // Tech-specific decisions //
    /////////////////////////////

    if (PvPRobo()) {
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

        if (PvPTechBeforeRange()) {
          // This strategy demands a ton of gas; we can't afford the Observer
          getObservatory = false
          getObservers = false
        } else if (enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
          // These builds generally let us rule out DT entirely
          getObservatory = false
          getObservers = false
        } else if (With.strategy.isFixedOpponent) {
          // Obs is what we're here for, so let's not get too cute
        } else if (trackRecordLacks(With.fingerprints.dtRush) || enemyStrategy(With.fingerprints.dragoonRange, With.fingerprints.twoGate, With.fingerprints.proxyGateway)) {
          if (enemyRecentStrategy(With.fingerprints.fourGateGoon, With.fingerprints.threeGateGoon) && ! enemyRecentStrategy(With.fingerprints.dtRush)) {
            getObservatory = false
            getObservers = false
          } else {
            getObservatory = roll("SpeculativeObservatory", if (trackRecordLacks(With.fingerprints.fourGateGoon)) 0.8 else 0.5)
            getObservers = getObservatory && roll("SpeculativeObservers", if (trackRecordLacks(With.fingerprints.fourGateGoon)) 1.0 else 0.75) // So the probability of obs is the *joint* probability
          }
        }
      }

      shouldExpand = unitsComplete(Protoss.Gateway) >= 2 && unitsComplete(Protoss.Reaver) > 0
      shouldExpand &&= safeToMoveOut
      shouldExpand &&= (
        (With.fingerprints.dtRush() && unitsComplete(Protoss.Observer) > 0)
        || (PvPIdeas.enemyLowUnitStrategy && unitsComplete(Protoss.Reaver) > 0))
      shouldExpand &&= ! PvPTechBeforeRange()
      shouldExpand ||= unitsComplete(Protoss.Reaver) >= 2
      shouldExpand ||= unitsComplete(IsWarrior) >= 20 && safeToMoveOut
    } else if (PvPDT()) {
      timeToStartCannons = PvPDTDefense.expectedDTArrivalFrame - Protoss.PhotonCannon.buildFrames + Protoss.Pylon.buildFrames + Seconds(15)()
      // Look for reasons to avoid getting cannons
      if (enemyDarkTemplarLikely) {
        getCannons = true
      } else {
        getCannons = units(Protoss.TemplarArchives) > 0
        getCannons &&= With.frame >= timeToStartCannons
        getCannons &&= safeAtHome
        getCannons &&= ! enemyRobo
        getCannons &&= ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
        getCannons &&= roll("DTSkipCannons", if (enemyRecentStrategy(With.fingerprints.dtRush)) 0.2 else 0.5)
        getCannons ||= With.fingerprints.dtRush()
        getCannons ||= enemyDarkTemplarLikely
        getCannons &&= ! With.units.existsOurs(Protoss.RoboticsFacility)
      }
      shouldExpand = unitsComplete(Protoss.DarkTemplar) > 0 || (safeToMoveOut && units(Protoss.DarkTemplar) > 0) || (upgradeComplete(Protoss.ZealotSpeed) && unitsComplete(IsWarrior) >= 20)
    } else if (PvP3GateGoon()) {
      shouldExpand = unitsComplete(Protoss.Gateway) >= 3 && unitsComplete(IsWarrior) >= 6
    } else if (PvP4GateGoon()) {
      shouldExpand = unitsComplete(IsWarrior) >= (if (safeToMoveOut) 20 else 28)
    }
    shouldExpand &&= ! With.fingerprints.dtRush() || unitsComplete(Protoss.Observer, Protoss.PhotonCannon) > 0
    shouldExpand &&= ! With.fingerprints.dtRush() || (units(Protoss.Observer, Protoss.PhotonCannon) > 0 && enemies(Protoss.DarkTemplar) == 0)

    // Attack when we reach an attack timing
    shouldAttack = PvPIdeas.shouldAttack
    // Don't attack if we're also dropping
    shouldAttack &&= ! upgradeStarted(Protoss.ShuttleSpeed)
    // Don't attack with just Gateway units if doing tech build vs non-tech one gate core
    shouldAttack &&= ! (employing(PvPGateCoreTech, PvPTechBeforeRange)
      && units(Protoss.CitadelOfAdun, Protoss.RoboticsFacility) > 0
      && unitsComplete(Protoss.DarkTemplar) == 0
      && unitsComplete(Protoss.Reaver) * unitsComplete(Protoss.Shuttle) == 0
      && (With.fingerprints.oneGateCore() || enemyHasUpgrade(Protoss.DragoonRange))
      && ! PvPIdeas.enemyLowUnitStrategy)
    // Push out to take our natural
    shouldAttack ||= shouldExpand
    // 2-Gate vs 1-Gate core needs to wait until range before venturing out again, to avoid rangeless goons fighting ranged goons
    shouldAttack &&= ! (With.frame > GameTime(5, 10)()
      && PvP1012()
      && (With.fingerprints.oneGateCore() || enemyHasUpgrade(Protoss.DragoonRange))
      && ! upgradeComplete(Protoss.DragoonRange)
      && unitsComplete(Protoss.DarkTemplar, Protoss.Reaver) == 0)
    // Ensure that committed Zealots keep wanting to attack
    shouldAttack ||= With.units.ours.exists(u => u.agent.commit) && With.frame < Minutes(5)()
    shouldHarass = upgradeStarted(Protoss.ShuttleSpeed) && unitsComplete(Protoss.Reaver) > 1

    // Chill vs. 2-Gate until we're ready to defend
    if ( ! PvP1012() && With.fingerprints.twoGate() && unitsEver(IsAll(Protoss.Dragoon, IsComplete)) == 0) {
      aggression(0.6)
    }

    oversaturate = units(Protoss.Reaver) > 0
    oversaturate ||= PvPDT()
    oversaturate ||= enemyBases > 1
    oversaturate ||= enemyRobo
    oversaturate ||= enemyDarkTemplarLikely
    oversaturate &&= ! speedlotAttack
    oversaturate &&= ! employing(PvP3GateGoon, PvP4GateGoon)

    /////////////
    // Logging //
    /////////////

    if (PvPGateCoreTech() || PvPGateCoreGate()) {
      if (zBeforeCore) {
        (if (zAfterCore) status("ZCoreZ") else status("ZCore"))
      } else {
        (if (zAfterCore) status("CoreZ") else status("NZCore"))
      }
    }
    if (sevenZealot) status("SevenZealots")
    if (commitZealots) status("CommitZealots")
    if (shuttleFirst) status("ShuttleFirst")
    if (getObservers) status("Obs")
    if (getObservatory) status("Observatory")
    if (PvPDT()) status(f"Cannon@${Frames(timeToStartCannons)}")
    if (getCannons) status("Cannons")
    if (speedlotAttack) status("Speedlot")
    if (shouldAttack) status("Attack")
    if (shouldHarass) status("Harass")
    if (shouldExpand) status("ExpandNow")

    if (shouldAttack) { attack() }
    if (shouldHarass) { harass() }

    ////////////////////////////
    // Emergency DT reactions //
    ////////////////////////////

    if (units(Protoss.CyberneticsCore) > 0 && enemyDarkTemplarLikely) {
      if (PvPRobo()) {
        buildOrder(Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer))
      } else {
        PvPDTDefense.reactToDarkTemplarEmergencies()
      }
    }

    //////////////
    // Scouting //
    //////////////

    if (enemies(Protoss.Dragoon) == 0 && ! With.fingerprints.proxyGateway()) {
      if (PvP1012()) {
        if ( ! foundEnemyBase && ! PvPIdeas.attackFirstZealot) {
          scoutOn(Protoss.Gateway, quantity = 2)
        }
      } else if (starts > 3) {
        scoutOn(Protoss.Gateway)
      } else if ( ! zBeforeCore || ! PvPIdeas.attackFirstZealot) {
        scoutOn(Protoss.CyberneticsCore)
      }
    }

    /////////////////
    // Zealot rush //
    /////////////////

    if (PvP1012()) {
      if (enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
        || enemies(Protoss.Zealot) > Math.min(unitsComplete(Protoss.Zealot), 2)) {
        //With.blackboard.pushKiters.set(false)
        With.units.ours.foreach(_.agent.commit = false)
      } else if (frame < GameTime(4, 15)() && enemiesComplete(Protoss.PhotonCannon) == 0) {
        // Wait until we have at least three Zealots together; then go in hard
        aggression(0.75)
        val zealots = With.units.ours.filter(u => Protoss.Zealot(u) && u.battle.exists(_.us.units.count(Protoss.Zealot) > 2)).toVector
        commitZealots ||= zealots.size >= (if (PvP3Zealot()) 3 else 5)
        if (commitZealots) {
          With.units.ours.filter(Protoss.Zealot).filter(_.complete).foreach(_.agent.commit = true)
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

    /////////////////////////
    // React against proxy //
    /////////////////////////

    if (With.fingerprints.proxyGateway() && With.frame < Minutes(5)() && unitsComplete(IsWarrior) < 7) {
      pumpSupply()
      pumpWorkers()
      if (units(Protoss.Gateway) < 2) {
        cancelIncomplete(Protoss.Assimilator)
        cancelIncomplete(Protoss.CyberneticsCore)
        cancelOrders(Protoss.CyberneticsCore)
        gasWorkerCeiling(0)
      } else if (units(Protoss.CyberneticsCore) == 0) {
        gasWorkerCeiling(1)
      } else if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        gasWorkerCeiling(2)
      }
      get(2, Protoss.Gateway)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
      get(Protoss.ShieldBattery)
      get(Protoss.Assimilator)
      get(Protoss.CyberneticsCore)
      get(3, Protoss.Gateway)
      get(Protoss.DragoonRange)
      return
    }

    ////////////
    // 2-Gate //
    ////////////

    if (PvP1012()) { // https://liquipedia.net/starcraft/2_Gate_(vs._Protoss)
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

      if (PvP5Zealot()) { // https://tl.net/forum/bw-strategy/380852-pvp-2-gate-5-zealot-expand
        buildOrder(
          Get(16, Protoss.Probe),
          Get(3, Protoss.Pylon),
          Get(17, Protoss.Probe),
          Get(5, Protoss.Zealot),
          Get(18, Protoss.Probe))
        if (With.fingerprints.proxyGateway()) {
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
        } else if (PvP4GateGoon()) {
          get(4, Protoss.Gateway)
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
          if (PvPGateCoreGate()) { // https://liquipedia.net/starcraft/2_Gate_Reaver_(vs._Protoss)
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
              Get(21, Protoss.Probe),
              Get(4, Protoss.Dragoon))
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
            Get(Protoss.Dragoon))

          ///////////////////////
          // Robo before range //
          ///////////////////////

          if (PvPTechBeforeRange()) {
            buildOrder(
              Get(17, Protoss.Probe),
              Get(Protoss.RoboticsFacility),
              Get(18, Protoss.Probe),
              Get(2, Protoss.Dragoon),
              Get(3, Protoss.Pylon),
              Get(19, Protoss.Probe),
              Get(3, Protoss.Dragoon))

          /////////////////
          // Range-first //
          /////////////////

          } else {
            buildOrder(
              Get(Protoss.DragoonRange),
              Get(17, Protoss.Probe))
            if (PvPGateCoreGate()) {
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
          }
        } else {
          buildOrder(
            Get(15, Protoss.Probe),
            Get(2, Protoss.Pylon),
            Get(17, Protoss.Probe),
            Get(Protoss.Dragoon),
            Get(Protoss.DragoonRange),
            Get(18, Protoss.Probe),
            Get(3, Protoss.Pylon),
            Get(19, Protoss.Probe),
            Get(2, Protoss.Dragoon),
            Get(20, Protoss.Probe))
        }
      }
    }
  }

  def executeMain(): Unit = {

    gasLimitCeiling(350)
    if (zBeforeCore && employing(PvP3GateGoon, PvP4GateGoon) && unitsComplete(Protoss.CyberneticsCore) < 1) {
      gasWorkerCeiling(1)
    }
    if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
      gasWorkerCeiling(2)
    }

    ////////////////////////
    // Transition to tech //
    ////////////////////////

    // The build order should have requested all of these, but just in case:
    buildOrder(Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore))
    if (PvPGateCoreGate() || PvP1012()) { get(2, Protoss.Gateway) }
    buildOrder(Get(Protoss.Dragoon))
    if ( ! PvPTechBeforeRange()) { get(Protoss.DragoonRange) }

    //////////
    // Tech //
    //////////

    if (PvPRobo()) {
      get(Protoss.RoboticsFacility)
      if (getObservers || getObservatory) {
        if (enemyDarkTemplarLikely && units(Protoss.Observer) == 0) {
          if (units(Protoss.Observatory) == 0) {
            if (gas < 100) {
              cancelIncomplete(Protoss.RoboticsSupportBay)
              cancelOrders(Protoss.RoboticsSupportBay)
            }
          } else if (unitsComplete(Protoss.Observatory) > 0 && units(Protoss.Observer) == 0) {
            cancelIncomplete(Protoss.Shuttle, Protoss.Reaver)
          }
        }
        get(Protoss.Observatory)
        if (With.fingerprints.dtRush()) {
          get(Protoss.ObserverSpeed)
        }
        if (units(Protoss.Observer) > 0) {
          get(Protoss.RoboticsSupportBay)
        }
      } else {
        if ( ! getObservatory) {
          cancelIncomplete(Protoss.Observatory)
        }
        cancelIncomplete(Protoss.Observer)
        get(Protoss.RoboticsSupportBay)
      }
      if (PvPTechBeforeRange() && ! getObservers) {
        buildOrder(
          Get(Protoss.Reaver),
          Get(Protoss.ShuttleSpeed),
          Get(Protoss.Shuttle))
        pump(Protoss.Reaver)
      } else {
        trainRoboUnits()
      }

      if (units(Protoss.Reaver) > 1) {
        get(Protoss.DragoonRange)
      }
      if (shouldExpand && ! With.geography.ourNatural.units.exists(u =>
        u.isEnemy
        && u.canAttackGround
        // Distance check in case map has a degenerate natural
        && u.pixelDistanceCenter(With.geography.ourNatural.townHallArea.center) < 32 * 12)) {
        requireMiningBases(2)
      }

      trainGatewayUnits()
      get(2, Protoss.Gateway)
      get(Protoss.DragoonRange)
      get(3, Protoss.Gateway)
    } else if (speedlotAttack) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
      if (getCannons) { buildCannonsAtNatural(2, PlaceLabels.DefendEntrance) }
      if (shouldExpand) { requireMiningBases(2) }
      SwapIf(
        safeAtHome && With.scouting.enemyProgress < 0.5,
        () => trainGatewayUnits(),
        () => get(5, Protoss.Gateway))
    } else if (PvPDT()) {
      if (getCannons) { buildCannonsAtNatural(2, PlaceLabels.DefendEntrance) }
      if ( ! enemyHasShown(Protoss.Observer, Protoss.Observatory)) {
        buildOrder(Get(Math.min(2, units(Protoss.Gateway)), Protoss.DarkTemplar))
      }
      trainGatewayUnits()
      get(Protoss.CitadelOfAdun)
      get(Protoss.TemplarArchives)
      get(2, Protoss.Gateway)
      if (shouldExpand) { requireMiningBases(2) }
    } else if (PvP3GateGoon()) {
      if (shouldExpand) { requireMiningBases(2) }
      buildOrder(Get(2, Protoss.Dragoon))
      get(3, Protoss.Gateway)
      trainGatewayUnits()
      buildOrder(Get(8, Protoss.Dragoon))
      requireMiningBases(2)
    } else { // 4-Gate Goon
      if (shouldExpand) { requireMiningBases(2) }
      buildOrder(Get(2, Protoss.Dragoon))
      get(4, Protoss.Gateway)
      trainGatewayUnits()
      buildOrder(Get(10, Protoss.Dragoon))
    }
    SwapIf(
      oversaturate,
      () => get(4, Protoss.Gateway),
      () => pumpWorkers(oversaturate = true))
    requireMiningBases(2)
  }

  private def trainRoboUnits(): Unit = {
    if (getObservers) {
      pump(Protoss.Observer, 2)
      if (With.fingerprints.dtRush()) pump(Protoss.Observer, 2)
    }
    if (shuttleFirst) buildOrder(Get(Protoss.Shuttle))
    if (units(Protoss.Reaver) >= 3) pumpShuttleAndReavers() else pump(Protoss.Reaver)
  }

  private def trainGatewayUnits(): Unit = {
    if (zAfterCore && zBeforeCore) buildOrder(Get(2, Protoss.Zealot))
    else if (zAfterCore || zBeforeCore) buildOrder(Get(Protoss.Zealot))
    buildOrder(Get(Protoss.Dragoon))
    if (upgradeComplete(Protoss.ZealotSpeed, 1, 2 * Protoss.Zealot.buildFrames)) {
      pump(Protoss.Dragoon, maximumConcurrently = 2)
      pump(Protoss.Zealot, 12)
    }
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
  }
}