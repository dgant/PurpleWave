package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat}
import Planning.Plans.Placement.BuildCannonsAtNatural
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Strategery._
import Utilities.{GameTime, Minutes}

class PvPOpening extends GameplanImperative {

  var complete: Boolean = false
  // General properties
  var shouldExpand: Boolean = false
  var shouldAttack: Boolean = false
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

  override def activated: Boolean = employing(PvPRobo, PvPDT, PvP3GateGoon, PvP4GateGoon)
  override def completed: Boolean = { complete ||= bases > 1; complete }

  val buildCannonsAtNatural = new BuildCannonsAtNatural(2)
  val reactToDTEmergencies = new OldPvPIdeas.ReactToDarkTemplarEmergencies
  override def executeBuild(): Unit = {

    /////////////////////
    // Update strategy //
    /////////////////////

    // Last minute COG2021 hack to ensure we can use GateCoreGate as a default
    if (employing(PvPGateCoreGate) && With.strategy.isRamped) {
      PvPGateCoreGate.swapOut()
      PvPGateCoreTech.swapIn()
    }

    if (units(Protoss.Assimilator) == 0) {
      if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon) && ! enemyStrategy(With.fingerprints.cannonRush)) {
        PvP1012.swapIn()
        PvPGateCoreTech.swapOut()
        PvPGateCoreGate.swapOut()
        PvPTechBeforeRange.swapOut()
      }
    }
    if (employing(PvP1012)) {
      if (units(Protoss.Assimilator) == 0) {
        PvP3Zealot.activate()
        PvP5Zealot.activate()
        // TODO: Against 10-12 it's okay to stay 3-Zealot. We only need 5-Zealot vs 9-9.
        if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst, With.fingerprints.gasSteal)) {
          PvP5Zealot.swapIn()
          PvP3Zealot.swapOut()
        }
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        sevenZealot = employing(PvP5Zealot)
        sevenZealot &&= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
      }
    } else {
      if (units(Protoss.CyberneticsCore) == 0) {
        zBeforeCore = With.geography.startLocations.size < 3
        zBeforeCore &&= ! employing(PvPDT)
        zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zBeforeCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zBeforeCore ||= employing(PvPGateCoreGate, PvP3GateGoon, PvP4GateGoon)
        zBeforeCore &&= ! employing(PvPTechBeforeRange)
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        zAfterCore = zBeforeCore
        zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zAfterCore ||= enemyRecentStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
        zAfterCore ||= employing(PvPGateCoreGate, PvPTechBeforeRange, PvPDT)
      }
      if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility) < 1 && units(Protoss.CitadelOfAdun) < 1) {
        if (enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst)) {
          PvPGateCoreGate.swapIn()
          PvPGateCoreTech.swapOut()
          PvPTechBeforeRange.swapOut()
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
    if (employing(PvP1012)
      && units(Protoss.Gateway) == 0
      && ! enemyRecentStrategy(With.fingerprints.proxyGateway) && Seq(Arcadia, Aztec, Benzene, Longinus, MatchPoint, Heartbreak, Roadkill).exists(_.matches)) {
      PvP1012.swapOut()
      PvP3Zealot.swapOut()
      PvP5Zealot.swapOut()
      (if (Seq(Heartbreak, Roadkill).exists(_.matches) || roll("1012ToGateCoreGate", 0.35)) PvPGateCoreGate else PvPGateCoreTech).swapIn()
    }
    // If we catch them going Robo against our DT, go goon-only
    if (employing(PvPDT) && (enemyRobo || enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe))) {
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
    if (employing(PvPRobo)
      && upgradeStarted(Protoss.DragoonRange)
      && units(Protoss.RoboticsFacility) == 0
      && enemies(Protoss.CitadelOfAdun) == 0
      && trackRecordLacks(With.fingerprints.dtRush)) {

      // 4-Gating quickly becomes a lot less appealing with more DT in the mix.
      if (employing(PvPRobo)
        && trackRecordLacks(With.fingerprints.robo)) {
        if (roll("SwapRoboIntoDT", if (enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) 0.6 else 0.3)) {
          PvPRobo.swapOut()
          PvPDT.swapIn()
        }
      }
      // 3/4-Gate Goon are advantaged against most Robo variants.
      // But we don't want to make this switch too predictably, as it's abusable.
      if (employing(PvPRobo) && enemyRecentStrategy(With.fingerprints.robo)) {
        if (roll("SwapRoboInto3Gate", if (enemyStrategy(With.fingerprints.robo)) 0.35 else 0.2)) {
          PvPRobo.swapOut()
          PvP3GateGoon.swapIn()
        } else if (roll("SwapRoboInto4Gate", if (enemyStrategy(With.fingerprints.robo)) 0.35 else 0.2)) {
          PvPRobo.swapOut()
          PvP4GateGoon.swapIn()
        }
      }
    }
    // Oops. We let them scout our DT rush. Maybe we can use it to our advantage.
    if (employing(PvPDT)
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

        if (employing(PvPTechBeforeRange)) {
          // This strategy demands a ton of gas; we can't afford the Observer
          getObservatory = false
          getObservers = false
        } else if (enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
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

      shouldExpand = unitsComplete(Protoss.Gateway) >= 2 && unitsComplete(Protoss.Reaver) > 0
      shouldExpand &&= safeToMoveOut
      shouldExpand &&= (
        enemyStrategy(With.fingerprints.dtRush) && unitsComplete(Protoss.Observer) > 0
        || PvPIdeas.enemyLowUnitStrategy && unitsComplete(Protoss.Reaver) > 0)
      shouldExpand &&= ! employing(PvPTechBeforeRange)
      shouldExpand ||= unitsComplete(Protoss.Reaver) >= 2
    } else if (employing(PvPDT)) {
      // Super-fast DT finishes 5:12 and thus arrives at the natural around 5:45
      // Example: http://www.openbw.com/replay-viewer/?rep=https://data.basil-ladder.net/bots/MegaBot2017/MegaBot2017%20vs%20Florian%20Richoux%20Heartbreak%20Ridge%20CTR_EA637F71.rep
      // Pylon + Forge + Cannon takes 1:15
      // So we need to start the cannon process no later than 4:30 (adding some time as a buffer for construction delays)
      // We can delay this based on things we've seen
      timeToStartCannons = GameTime(4, 30)()
      // TODO: Delay if they went Zealot-first into core
      if (enemyStrategy(With.fingerprints.twoGate)) timeToStartCannons += GameTime(1, 10)()
      if (enemyStrategy(With.fingerprints.dragoonRange)) timeToStartCannons += GameTime(0, 30)()
      // Look for reasons to avoid getting cannons
      if (enemyDarkTemplarLikely) {
        getCannons = true
      } else {
        getCannons = units(Protoss.TemplarArchives) > 0
        getCannons &&= With.frame >= timeToStartCannons
        getCannons &&= safeAtHome
        getCannons &&= ! enemyRobo
        getCannons &&= enemyBases < 2
        getCannons &&= ! enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
        getCannons &&= roll("DTSkipCannons", if (enemyRecentStrategy(With.fingerprints.dtRush)) 0.2 else 0.5)
      }
      shouldExpand = unitsComplete(Protoss.DarkTemplar) > 0 || (safeToMoveOut && units(Protoss.DarkTemplar) > 0) || (upgradeComplete(Protoss.ZealotSpeed) && unitsComplete(MatchWarriors) >= 20)
    } else if (employing(PvP3GateGoon)) {
      shouldExpand = unitsComplete(Protoss.Gateway) >= 3 && unitsComplete(MatchWarriors) >= 6
    } else if (employing(PvP4GateGoon)) {
      shouldExpand = unitsComplete(MatchWarriors) >= (if (safeToMoveOut) 20 else 28)
    }
    shouldExpand &&= ! With.fingerprints.dtRush.matches || unitsComplete(Protoss.Observer, Protoss.PhotonCannon) > 0
    shouldExpand &&= ! With.fingerprints.dtRush.matches || (units(Protoss.Observer, Protoss.PhotonCannon) > 0 && enemies(Protoss.DarkTemplar) == 0)

    // Attack when we reach an attack timing
    shouldAttack = PvPIdeas.shouldAttack
    // Push out to take our natural
    shouldAttack ||= shouldExpand
    // Don't attack if we're also dropping (unless it's time to take our natural)
    shouldAttack &&= ( ! upgradeStarted(Protoss.ShuttleSpeed) || shouldExpand)
    // 2-Gate vs 1-Gate core needs to wait until range before venturing out again, to avoid rangeless goons fighting ranged goons
    shouldAttack &&= ! (With.frame > GameTime(5, 10)()
      && employing(PvP1012)
      && enemyStrategy(With.fingerprints.oneGateCore)
      && ! upgradeComplete(Protoss.DragoonRange)
      && unitsComplete(Protoss.DarkTemplar, Protoss.Reaver) == 0)
    // Ensure that committed Zealots keep wanting to attack
    shouldAttack ||= With.units.ours.exists(u => u.agent.commit) && With.frame < Minutes(5)()

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
    if (sevenZealot) status("SevenZealots")
    if (commitZealots) status("CommitZealots")
    if (shuttleFirst) status("ShuttleFirst")
    if (getObservers) status("Obs")
    if (getObservatory) status("Observatory")
    if (employing(PvPDT)) status(f"Cannon@${new GameTime(timeToStartCannons)}")
    if (getCannons) status("Cannons")
    if (speedlotAttack) status("Speedlot")
    if (shouldAttack) status("Attack")
    if (shouldExpand) status("ExpandNow")
    oversaturate = (units(Protoss.Reaver) > 0 || units(Protoss.DarkTemplar) > 0 || minerals >= 450) && ! speedlotAttack && ! employing(PvP3GateGoon, PvP4GateGoon)
    if (shouldAttack) { attack() }
    if (upgradeStarted(Protoss.ShuttleSpeed)) { harass() }

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
      } else if ( ! zBeforeCore || ! PvPIdeas.attackFirstZealot) {
        scoutOn(Protoss.CyberneticsCore)
      }
    }

    /////////////////
    // Zealot rush //
    /////////////////

    if (employing(PvP1012)) {
      if (enemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)
        || enemies(Protoss.Zealot) > Math.min(unitsComplete(Protoss.Zealot), 2)) {
        //With.blackboard.pushKiters.set(false)
        With.units.ours.foreach(_.agent.commit = false)
      } else if (frame < GameTime(4, 15)() && enemiesComplete(Protoss.PhotonCannon) == 0) {
        // Wait until we have at least three Zealots together; then go in hard
        aggression(0.75)
        val zealots = With.units.ours.filter(u => Protoss.Zealot(u) && u.battle.exists(_.us.units.count(Protoss.Zealot) > 2)).toVector
        commitZealots ||= zealots.size > 2
        if (commitZealots) {
          //With.blackboard.pushKiters.set(true)
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
        } else if (employing(PvP4GateGoon)) {
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

          if (employing(PvPTechBeforeRange)) {
            buildOrder(
              Get(17, Protoss.Probe),
              Get(Protoss.RoboticsFacility),
              Get(18, Protoss.Probe),
              Get(2, Protoss.Dragoon),
              Get(19, Protoss.Probe),
              Get(3, Protoss.Pylon))

          /////////////////
          // Range-first //
          /////////////////

          } else {
            buildOrder(
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
    if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
      gasWorkerCeiling(2)
    }

    ////////////////////////
    // Transition to tech //
    ////////////////////////

    // The build order should have requested all of these, but just in case:
    buildOrder(Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore))
    if (employing(PvPGateCoreGate) || employing(PvP1012)) { get(2, Protoss.Gateway) }
    buildOrder(Get(Protoss.Dragoon))
    if ( ! employing(PvPTechBeforeRange)) { get(Protoss.DragoonRange) }

    //////////
    // Tech //
    //////////

    if (employing(PvPRobo)) {
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
        if (With.fingerprints.dtRush.matches) {
          get(Protoss.ObserverSpeed)
        }
        if (units(Protoss.Observer) > 0) {
          get(Protoss.RoboticsSupportBay)
        }
      } else {
        if ( ! getObservatory) { cancelIncomplete(Protoss.Observatory) }
        cancelIncomplete(Protoss.Observer)
        get(Protoss.RoboticsSupportBay)
      }

      if (employing(PvPTechBeforeRange) && ! getObservers) {
        get(Protoss.ShuttleSpeed)
        pump(Protoss.Reaver, 1)
        if (unitsEver(Protoss.Shuttle) == 0) {
          pump(Protoss.Shuttle, 1) // Conditional pump() instead of buildOrder to ensure smooth usage of robo
        }
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
        && u.pixelDistanceCenter(With.geography.ourNatural.townHallArea.center) < 32 * 15)) {
        requireMiningBases(2)
      }

      trainGatewayUnits()
      get(2, Protoss.Gateway)
      get(Protoss.DragoonRange)
      get(3, Protoss.Gateway)
    } else if (speedlotAttack) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
      if (getCannons) { buildCannonsAtNatural.update() }
      if (shouldExpand) { requireMiningBases(2) }
      if (safeAtHome && With.scouting.enemyProgress < 0.5) {
        get(5, Protoss.Gateway)
      }
      trainGatewayUnits()
      get(5, Protoss.Gateway)
    } else if (employing(PvPDT)) {
      get(Protoss.CitadelOfAdun)
      get(Protoss.TemplarArchives)
      buildOrder(Get(Protoss.DarkTemplar))
      if (getCannons) { buildCannonsAtNatural.update() }
      if (shouldExpand) { requireMiningBases(2) }
      if ( ! enemyRobo) pump(Protoss.DarkTemplar, 1)
      trainGatewayUnits()
      if (units(Protoss.TemplarArchives) > 0) {
        requireMiningBases(2)
      }
    } else if (employing(PvP3GateGoon)) {
      if (shouldExpand) { requireMiningBases(2) }
      buildOrder(Get(2, Protoss.Dragoon))
      get(3, Protoss.Gateway)
      trainGatewayUnits()
      buildOrder(Get(8, Protoss.Dragoon))
      if (unitsComplete(Protoss.Gateway) >= 3) { requireMiningBases(2) }
    } else {
      if (shouldExpand) { requireMiningBases(2) }
      buildOrder(Get(2, Protoss.Dragoon))
      get(4, Protoss.Gateway)
      trainGatewayUnits()
      buildOrder(Get(10, Protoss.Dragoon))
      if (unitsComplete(Protoss.Gateway) >= 4) { requireMiningBases(2) }
    }
    // Even for builds that shouldn't get 4 Gateways in theory,
    // our mineral mining is so efficient we keep winding up with lots of minerals anyway
    get(4, Protoss.Gateway)
  }

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
    if (upgradeComplete(Protoss.ZealotSpeed, 1, 2 * Protoss.Zealot.buildFrames)) {
      pump(Protoss.Dragoon, maximumConcurrently = 2)
      pump(Protoss.Zealot, 12)
      pump(Protoss.Dragoon)
    } else {
      pump(Protoss.Dragoon)
      if (
        (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate) && With.frame < Minutes(4)())
        || (gas < 42 && minerals >= 175 && ! employing(PvP3GateGoon, PvP4GateGoon))) {
        pump(Protoss.Zealot)
      }
    }
  }
}