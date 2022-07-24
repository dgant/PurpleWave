package Planning.Plans.GamePlans.Protoss.PvP

import Lifecycle.With
import Macro.Requests.RequestUnit
import Mathematics.Maff
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.{Enemy, Flat}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Strategery._
import Utilities.?
import Utilities.Time._
import Utilities.UnitFilters.{IsAll, IsComplete, IsWarrior}

class PvPOpening extends GameplanImperative {

  var complete          : Boolean = false
  var atTiming          : Boolean = false
  var noTiming          : Boolean = false
  var shouldExpand      : Boolean = false
  var shouldAttack      : Boolean = false
  var shouldHarass      : Boolean = false
  // 10-12
  var commitZealots     : Boolean = false
  var sevenZealot       : Boolean = false
  // 1 Gate Core
  var zBeforeCore       : Boolean = false
  var zAfterCore        : Boolean = false
  // Robo
  var getObservers      : Boolean = false
  var getObservatory    : Boolean = false
  var getReavers        : Boolean = false
  var shuttleFirst      : Boolean = false
  var shuttleSpeed      : Boolean = false
  // DT
  var swapOutOfDT       : Boolean = false
  var greedyDT          : Boolean = false

  override def activated: Boolean = true
  override def completed: Boolean = {
    complete ||= bases > 1;
    complete &&= ! PvPDT() || With.units.everOurs.exists(u => u.isOurs && u.complete && Protoss.DarkTemplar(u))
    complete
  }

  private def sneakyCitadel(): Unit = {
    if (scoutCleared) {
      get(Protoss.CitadelOfAdun)
      cancel(Protoss.AirDamage)
    } else if (units(Protoss.CitadelOfAdun) == 0) {
      if (With.units.ours.find(_.upgradeProducing.contains(Protoss.AirDamage)).exists(_.remainingUpgradeFrames < Seconds(5)())) {
        cancel(Protoss.AirDamage)
      } else if ( ! upgradeStarted(Protoss.DragoonRange)) {
        get(Protoss.AirDamage)
      }
    }
  }

  override def executeBuild(): Unit = {

    /////////////////////
    // Update strategy //
    /////////////////////

    // Swap into 2-Gate
    if (units(Protoss.Assimilator) == 0 && With.frame < Minutes(2)()) {
      if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon) && ! With.fingerprints.cannonRush()) {
        PvP1012.swapIn()
        PvPGateCoreRange.swapOut()
        PvPGateCoreGate.swapOut()
        PvPGateCoreTech.swapOut()
      }
    }
    if (PvP1012()) {
      if (units(Protoss.Assimilator) == 0) {
        PvP3Zealot()
        PvP5Zealot()
        if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99, With.fingerprints.nexusFirst, With.fingerprints.gasSteal)) {
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
        zBeforeCore = enemyRecentStrategy(With.fingerprints.twoGate99, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zBeforeCore &&= ! PvPGateCoreTech()
        zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zBeforeCore ||= With.fingerprints.twoGate()
        zBeforeCore ||= PvPGateCoreGate()
      }
      if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        zAfterCore = true
        zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.coreBeforeZ)
        zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal)
        zAfterCore ||= PvPGateCoreGate() || PvPGateCoreTech() || PvPDT()
      }
      if (units(Protoss.Gateway) < 2 && units(Protoss.RoboticsFacility, Protoss.CitadelOfAdun) < 1 && ! anyUpgradeStarted(Protoss.DragoonRange, Protoss.AirDamage)) {
        if (With.fingerprints.twoGate() || With.fingerprints.proxyGateway() || With.fingerprints.nexusFirst()) {
          PvPGateCoreRange.swapOut()
          PvPGateCoreTech.swapOut()
          PvPGateCoreGate.swapIn()
        } else if (With.fingerprints.cannonRush() || (With.fingerprints.earlyForge() && With.fingerprints.cannonRush.recently)) {
          PvP1012.swapOut()
          PvPGateCoreRange.swapOut()
          PvPGateCoreTech.swapIn()
          PvPRobo.swapIn()
          PvPDT.swapOut()
          PvPCoreExpand.swapOut()
          PvP3GateGoon.swapOut()
          PvP4GateGoon.swapOut()
        } else if (PvPGateCoreGate()) {
          PvPGateCoreGate.swapOut()
          if (With.strategy.isRamped) {
            PvPGateCoreTech.swapIn()
          } else {
            PvPGateCoreRange.swapIn()
          }
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
      (if (Seq(Heartbreak, Roadkill).exists(_()) || roll("1012ToGateCoreGate", 0.35)) PvPGateCoreGate else PvPGateCoreRange).swapIn()
    }
    // Goon+Obs is the strongest punishment against badly hidden DT openers.
    // A glimpse of Citadel doesn't sufficiently justify switching into Obs for its own sake,
    // as the Citadel could be a fake and the investment is a lot less than making even one Observer,
    // but a switch into full-blown Robotics at least lets us benefit from the investment if the Citadel was a fake
    if (employing(PvPCoreExpand, PvP3GateGoon, PvP4GateGoon) && enemies(Protoss.CitadelOfAdun) > 0 && units(Protoss.Gateway) < 3) {
      if (roll("SwapGateIntoRoboVsCitadel", 0.3)) {
        PvPCoreExpand.swapOut()
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
        && trackRecordLacks(With.fingerprints.robo)
        && roll("SwapRoboIntoDT", ?(enemyRecentStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon), 0.5, 0.3))) {
        // COG 2022: DT results are underwhelming and robo needs more testing
        //  PvPRobo.swapOut()
        //  PvPDT.swapIn()
      }
      // 3/4-Gate Goon are advantaged against most Robo variants.
      // But we don't want to make this switch too predictably, as it's abusable.
      if (PvPRobo()
        && ! With.strategy.isFixedOpponent
        && enemyRecentStrategy(With.fingerprints.robo) ) {
        if (roll("SwapRoboInto3Gate", ?(With.fingerprints.robo(), 0.35, 0.2))) {
          PvPRobo.swapOut()
          PvP3GateGoon.swapIn()
        } else if (roll("SwapRoboInto4Gate", ?(With.fingerprints.robo(), 0.35,  0.2))) {
          PvPRobo.swapOut()
          PvP4GateGoon.swapIn()
        }
      }
    }
    // If we catch them going Robo against our DT, go goon-only
    if (PvPDT() && unitsComplete(Protoss.TemplarArchives) == 0 && (enemyRobo || enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe))) {
      PvPDT.swapOut()
      if (roll("SwapDTIntoExpand", 0.6)) {
        PvPCoreExpand.swapIn()
      } else if (roll("SwapDTInto4Gate", 0.5)) {
        PvP4GateGoon.swapIn()
      } else {
        PvP3GateGoon.swapIn()
      }
    }
    if ( ! PvPDT()) {
      cancel(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
      if (enemies(Protoss.Observer) > 0 || enemies(Protoss.Observatory) > 0) {
        cancel(Protoss.DarkTemplar)
      }
    }
    // Oops. We let them scout our DT rush. Maybe we switch tech advantageously
    if (PvPDT() && scoutCleared && unitsComplete(Protoss.Dragoon) > 0) {
      val caught = With.units.ours.filter(u => u.isAny(Protoss.CitadelOfAdun, Protoss.TemplarArchives) && u.knownToOpponents && ! u.visibleToOpponents).toVector
      val archivesComplete  = caught.exists(c => Protoss.TemplarArchives(c) &&    c.complete)
      val archivesStarted   = caught.exists(c => Protoss.TemplarArchives(c) &&  ! c.complete)
      val citadelComplete   = caught.exists(c => Protoss.CitadelOfAdun(c)   &&    c.complete)
      val maybeMirror       = enemyRecentStrategy(With.fingerprints.dtRush) && ! With.fingerprints.robo()
      val mirrorMultiplier  = ?(maybeMirror, 0.25, 1.0)
      if (archivesComplete) {
        // We've come too far; no point switching
      } else if (archivesStarted) {
        swapOutOfDT = roll("DTSwap", 0.65 * mirrorMultiplier)
      } else if (citadelComplete) {
        swapOutOfDT = roll("DTSwap", 0.75 * mirrorMultiplier)
      } else if (caught.nonEmpty) {
        swapOutOfDT = roll("DTSwap", 0.85 * mirrorMultiplier)
      }
      if (swapOutOfDT) {
        PvPDT.swapOut()
        if (maybeMirror && roll("DTToRobo", 0.6)) {
          PvPRobo.swapIn()
        } else if ( ! citadelComplete && roll("DTToFE", 0.6)) {
          PvPCoreExpand.swapIn()
        } else if (roll("DTTo4Gate", 0.4)) {
          PvP4GateGoon.swapIn()
        } else {
          PvP3GateGoon.swapIn()
        }
      }
    }
    if (swapOutOfDT) {
      cancel(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
    }

    /////////////////////////////
    // Tech-specific decisions //
    /////////////////////////////

    if (PvPRobo()) {
      getObservatory = true
      getObservers = true
      if (units(Protoss.RoboticsSupportBay, Protoss.Shuttle) == 0) {
        getReavers = ! With.fingerprints.dtRush()
        shuttleFirst = enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.nexusFirst)
      }
      if (enemyDarkTemplarLikely || enemies(Protoss.CitadelOfAdun) > 0) {
        shuttleFirst = false
      } else {
        // Look for reasons to avoid making an Observer.
        // Don't stop to check if we already started an Observatory or Observers
        // because we can cancel and switch out of them at any time.
        if (shuttleSpeed) {
          // This strategy demands a ton of gas; we can't afford the Observer
          getObservatory = false
          getObservers = false
        } else if (enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
          // These builds generally let us rule out DT entirely
          getObservatory = false
          getObservers = false
        } else if (With.strategy.isFixedOpponent) {
          // Obs is probably what we're here for, so let's not get too cute
        } else if (With.frame > GameTime(5, 15)() && ! With.fingerprints.dragoonRange()) {
          // If Dragoon range is supiciously absent we should prepare for DT
        } else if (trackRecordLacks(With.fingerprints.dtRush)) {
          getObservatory = false
          getObservers = false
        } else if (enemyRecentStrategy(With.fingerprints.fourGateGoon, With.fingerprints.threeGateGoon) && ! enemyRecentStrategy(With.fingerprints.dtRush)) {
          getObservatory = roll("SpeculativeObservatory",  0.2)
          getObservers = getObservatory
        } else {
          getObservatory  =                   roll("SpeculativeObservatory",  ?(trackRecordLacks(With.fingerprints.fourGateGoon), 0.8, 0.4))
          getObservers    = getObservatory && roll("SpeculativeObservers",    ?(trackRecordLacks(With.fingerprints.fourGateGoon), 1.0, 0.75)) // So the probability of obs is the *joint* probability
        }
      }
      shuttleSpeed = shuttleFirst && PvPGateCoreTech() && ! getObservatory && ! getObservers && units(Protoss.Observatory, Protoss.Observer) == 0 && roll("ShuttleSpeedRush", 0.0)
    } else if (PvPDT()) {
      greedyDT = units(Protoss.TemplarArchives) > 0 && ! enemyStrategy(With.fingerprints.twoGate, With.fingerprints.dtRush) && roll("DTGreedyExpand", ?(enemyRecentStrategy(With.fingerprints.dtRush), 0.0, 0.5))
    }

    // Identify when we reach an attack timing
    atTiming ||= PvP1012()          && unitsComplete(Protoss.Zealot) >= 3
    atTiming ||= PvPGateCoreGate()  && unitsComplete(Protoss.Dragoon) > enemies(Protoss.Dragoon)
    atTiming ||= PvP3GateGoon()     && unitsCompleteFor(Protoss.Dragoon.buildFrames, Protoss.Gateway) >= 3 && unitsComplete(IsWarrior) >= 6
    atTiming ||= PvP4GateGoon()     && unitsCompleteFor(Protoss.Dragoon.buildFrames, Protoss.Gateway) >= 4 && unitsComplete(IsWarrior) >= 6
    atTiming ||= PvPDT()            && unitsComplete(Protoss.DarkTemplar) > 0
    atTiming ||= PvPRobo()          && unitsComplete(Protoss.Reaver) * unitsComplete(Protoss.Shuttle) >= 2 && unitsComplete(IsWarrior) >= 8
    atTiming ||= unitsComplete(Protoss.Observer)  > 0 && With.fingerprints.dtRush()
    atTiming ||= unitsComplete(Protoss.Dragoon)   > 0 && With.fingerprints.proxyGateway()
    atTiming ||= unitsComplete(Protoss.Reaver)    > 0 && With.fingerprints.cannonRush()
    atTiming ||= enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.cannonRush)
    // There may not be a timing depending on what our opponent does,
    // or the timing window might close permanently.
    noTiming ||= PvP1012()          && enemyStrategy(With.fingerprints.twoGate)
    noTiming ||= PvPGateCoreGate()  && enemyHasUpgrade(Protoss.DragoonRange) && ! safeToMoveOut && enemyStrategy(With.fingerprints.twoGateGoon, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
    noTiming ||= PvP3GateGoon()     && enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
    noTiming ||= PvP4GateGoon()     && enemyStrategy(With.fingerprints.fourGateGoon)
    noTiming ||= PvPDT()            && enemiesComplete(Protoss.Observer, Protoss.PhotonCannon) > 0

    shouldAttack = atTiming && ! noTiming
    shouldAttack &&= safeToMoveOut
    shouldAttack &&= enemies(Protoss.DarkTemplar) == 0 || unitsComplete(Protoss.Observer) > 0
    shouldAttack ||= shouldExpand && ! With.scouting.weControlOurNatural // Push out to take our natural
    shouldAttack &&= ! (PvP1012() // 2-Gate vs 1-Gate Core needs to wait until range before venturing out again, to avoid rangeless goons fighting ranged goons
      && With.frame > GameTime(5, 10)()
      && (With.fingerprints.oneGateCore() || enemyHasUpgrade(Protoss.DragoonRange))
      && ! upgradeComplete(Protoss.DragoonRange)
      && unitsComplete(Protoss.DarkTemplar, Protoss.Reaver) == 0)
    shouldAttack ||= With.units.ours.exists(_.agent.commit) && With.frame < Minutes(5)() // Ensure that committed Zealots keep wanting to attack
    shouldHarass = upgradeStarted(Protoss.ShuttleSpeed) && unitsComplete(Protoss.Reaver) > 1

    if (PvPRobo()) {
      shouldExpand = unitsComplete(Protoss.Reaver) > 0 || ( ! getReavers && unitsComplete(IsWarrior) >= 6)
      shouldExpand &&= PvPIdeas.pvpSafeToMoveOut
      shouldExpand &&= ! shuttleSpeed
      shouldExpand &&= Protoss.DragoonRange()
      shouldExpand ||= unitsComplete(Protoss.Reaver) > 1
      shouldExpand ||= unitsComplete(IsWarrior) >= 12 && safeToMoveOut
    } else if (PvPDT()) {
      shouldExpand = atTiming
      shouldExpand ||= units(Protoss.DarkTemplar) > 0 && (PvPIdeas.pvpSafeAtHome || With.scouting.enemyProximity < 0.75)
      shouldExpand ||= greedyDT
    } else if (PvP3GateGoon()) {
      shouldExpand = atTiming
      shouldExpand &&= unitsComplete(IsWarrior) >= 6
      shouldExpand &&= PvPIdeas.pvpSafeAtHome
    } else if (PvP4GateGoon()) {
      shouldExpand = atTiming
      shouldExpand &&= unitsComplete(IsWarrior) >= ?(safeToMoveOut, ?(PvPIdeas.enemyContained, 14, 20), 28)
      shouldExpand ||= atTiming && ! noTiming && PvPIdeas.safeAtHome
    }

    // Chill vs. 2-Gate until we're ready to defend
    if ( ! PvP1012() && With.fingerprints.twoGate() && unitsEver(IsAll(Protoss.Dragoon, IsComplete)) == 0) {
      aggression(0.6)
    }

    /////////////
    // Logging //
    /////////////

    if (PvPGateCoreRange() || PvPGateCoreGate()) {
      if (zBeforeCore) {
        (if (zAfterCore) status("ZCoreZ") else status("ZCore"))
      } else {
        (if (zAfterCore) status("CoreZ") else status("NZCore"))
      }
    }
    if (sevenZealot)    status("SevenZealots")
    if (commitZealots)  status("CommitZealots")
    if (shuttleFirst)   status("ShuttleFirst")
    if (shuttleSpeed)   status("ShuttleSpeed")
    if (getObservers)   status("Obs")
    if (getObservatory) status("Observatory")
    if (greedyDT)       status("GreedyDT")
    if (shouldAttack)   status("Attack")
    if (shouldHarass)   status("Harass")
    if (shouldExpand)   status("Expand")

    if (shouldAttack) { attack() }
    if (shouldHarass) { harass() }

    ////////////////////////////
    // Emergency DT reactions //
    ////////////////////////////

    if ( ! greedyDT) {
      PvPIdeas.requireTimelyDetection()
    }

    //////////////
    // Scouting //
    //////////////

    if (enemies(Protoss.Dragoon) == 0
      && ! With.fingerprints.proxyGateway()
      && ! With.units.enemy.filter(Protoss.CyberneticsCore).exists(With.frame - _.completionFrame > Protoss.Dragoon.buildFrames - Seconds(5)())) {
      if (PvP1012()) {
        if ( ! foundEnemyBase && ! PvPIdeas.attackFirstZealot) {
          scoutOn(Protoss.Gateway, quantity = 2)
        }
      } else if (starts > 3 || enemyRecentStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99)) {
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

    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)

    ///////////////////////
    // React against 9-9 //
    ///////////////////////

    if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99) && With.frame < Minutes(5)() && unitsComplete(IsWarrior) < 7) {
      status("99Defense")
      gasLimitCeiling(200)
      if (units(Protoss.Gateway) < 2 || unitsComplete(Protoss.Probe) < 15) {
        cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.DragoonRange)
        gasWorkerCeiling(0)
      } else if (units(Protoss.CyberneticsCore) == 0) {
        gasWorkerCeiling(0)
      } else if (unitsComplete(Protoss.CyberneticsCore) == 0) {
        if (units(Protoss.Dragoon) == 0) {
          gasLimitCeiling(50)
        } else {
          gasWorkerCeiling(2)
        }
      }
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(10, Protoss.Probe)
      once(Protoss.Gateway)
      once(11, Protoss.Probe)
      once(2, Protoss.Gateway)
      pumpSupply()
      pumpWorkers(oversaturate = true)
      pump(Protoss.Dragoon)
      pump(Protoss.Zealot)
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
      once(12,  Protoss.Probe)
      once(2,   Protoss.Gateway)
      once(13,  Protoss.Probe)
      once(1,   Protoss.Zealot)
      once(2,   Protoss.Pylon)
      once(15,  Protoss.Probe)
      once(3,   Protoss.Zealot)

      ///////////////
      // 5+ Zealot //
      ///////////////

      if (PvP5Zealot()) { // https://tl.net/forum/bw-strategy/380852-pvp-2-gate-5-zealot-expand
        once(16,  Protoss.Probe)
        once(3,   Protoss.Pylon)
        once(17,  Protoss.Probe)
        once(5,   Protoss.Zealot)
        once(18,  Protoss.Probe)
        if (With.fingerprints.proxyGateway()) {
          pump(Protoss.Probe, 12)
          pumpRatio(Protoss.Zealot, 3, 5, Seq(Flat(2.0), Enemy(Protoss.Zealot, 1.0)))
          pump(Protoss.Probe, 18)
        }
        once(4, Protoss.Pylon)
        once(Protoss.Assimilator)
        once(19, Protoss.Probe)
        once(Protoss.CyberneticsCore)
        if (sevenZealot) {
          once(7, Protoss.Zealot)
        } else if (PvP4GateGoon()) {
          once(4, Protoss.Gateway)
        }
        once(21, Protoss.Probe)
        once(3, Protoss.Gateway)
        once(2, Protoss.Dragoon)
        once(Protoss.DragoonRange)

      //////////////
      // 3-Zealot //
      //////////////

      } else { // https://tl.net/forum/bw-strategy/567442-pvp-bonyth-style-2-gate-3-zealot-21-gas-guide
        once(Protoss.Assimilator)
        once(17, Protoss.Probe)
        once(Protoss.CyberneticsCore)
        once(18, Protoss.Probe)
        once(3, Protoss.Pylon)
        once(20, Protoss.Probe)
        once(4, Protoss.Pylon) // On paper this build requires losing the Zealots to free supply, but with mineral optimization we can easily afford the Pylon
        once(2, Protoss.Dragoon)
        once(21, Protoss.Probe)
        once(Protoss.DragoonRange)
        once(22, Protoss.Probe)
        once(3, Protoss.Gateway) // Also not in the build but we can afford it so let's
        once(4, Protoss.Dragoon)
        once(23, Protoss.Probe)
        once(5, Protoss.Pylon)
        once(24, Protoss.Probe)
        once(6, Protoss.Dragoon)
      }

    /////////////////
    // 1 Gate Core //
    /////////////////

    } else {
      once(12, Protoss.Probe)
      once(Protoss.Assimilator)
      once(13, Protoss.Probe)

      /////////////
      // 17 Core //
      /////////////

      if (zBeforeCore) { // https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)
        once(Protoss.Zealot)
        once(14, Protoss.Probe)
        once(2, Protoss.Pylon)
        once(15, Protoss.Probe)
        once(Protoss.CyberneticsCore)
        once(16, Protoss.Probe)
        if (zAfterCore) {
          once(2, Protoss.Zealot)
          if (PvPGateCoreGate()) { // https://liquipedia.net/starcraft/2_Gate_Reaver_(vs._Protoss
            once(18, Protoss.Probe)
            once(3, Protoss.Pylon)
            once(19, Protoss.Probe)
            once(Protoss.Dragoon)
            once(20, Protoss.Probe)
            once(2, Protoss.Gateway)
            once(21, Protoss.Probe)
            once(3, Protoss.Dragoon)
            once(3, Protoss.Dragoon)
            once(Protoss.DragoonRange)
            once(4, Protoss.Pylon)
            once(21, Protoss.Probe)
            once(4, Protoss.Dragoon)
          }
        }
        once(17, Protoss.Probe)

      /////////////
      // 13 Core //
      /////////////

      } else {
        // https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)
        once(Protoss.CyberneticsCore)
        if (zAfterCore) {
          // https://namu-wiki.translate.goog/w/21%ED%88%AC%EA%B2%8C%EC%9D%B4%ED%8A%B8?_x_tr_sl=ko&_x_tr_tl=en&_x_tr_hl=en&_x_tr_pto=ajax,sc,elem
          once(14, Protoss.Probe)
          once(Protoss.Zealot)
          once(2, Protoss.Pylon)
          once(16, Protoss.Probe)
          once(Protoss.Dragoon)

          ///////////////////////
          // Robo before range //
          ///////////////////////

          if (PvPGateCoreTech()) {
            once(17, Protoss.Probe)
            if (PvPDT()) sneakyCitadel() else if (PvPRobo()) once(Protoss.RoboticsFacility) else get(Protoss.DragoonRange)
            once(18, Protoss.Probe)
            once(2, Protoss.Dragoon)
            once(3, Protoss.Pylon)
            once(19, Protoss.Probe)
            once(3, Protoss.Dragoon)

          /////////////////
          // Range-first //
          /////////////////

          } else {
            once(Protoss.DragoonRange)
            once(17, Protoss.Probe)
            if (PvPGateCoreGate()) {
              once(2, Protoss.Gateway)
              once(2, Protoss.Dragoon)
              once(18, Protoss.Probe)
              once(3, Protoss.Pylon)
            } else {
              once(2, Protoss.Dragoon)
              once(18, Protoss.Probe)
            }
          }
        } else {
          once(15, Protoss.Probe)
          once(2, Protoss.Pylon)
          once(17, Protoss.Probe)
          once(Protoss.Dragoon)
          once(Protoss.DragoonRange)
          once(18, Protoss.Probe)
          once(3, Protoss.Pylon)
          once(19, Protoss.Probe)
          once(2, Protoss.Dragoon)
          once(20, Protoss.Probe)
        }
      }
    }
  }

  def executeMain(): Unit = {

    if (gasCapsUntouched) {
      gasLimitCeiling(350)
      if (zBeforeCore && units(Protoss.CyberneticsCore) < 1) {
        gasWorkerCeiling(1)
      } else if (zBeforeCore && unitsComplete(Protoss.CyberneticsCore) < 1) {
        gasWorkerCeiling(2)
      } else if (employing(PvPCoreExpand, PvP3GateGoon, PvP4GateGoon) && units(Protoss.Gateway) < ?(PvP4GateGoon(), 4, 3)) {
        gasWorkerCeiling(2)
        gasLimitCeiling(250)
      }
      if (PvPCoreExpand() && (upgradeStarted(Protoss.DragoonRange) || gas >= 200)) {
        gasWorkerCeiling(1)
        gasLimitCeiling(100)
      }
    }

    ////////////////////////
    // Transition to tech //
    ////////////////////////

    // The build order should have requested all of these, but just in case:
    once(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    if (PvPGateCoreGate() || PvP1012()) { once(2, Protoss.Gateway) }
    once(Protoss.Dragoon)
    if ( ! PvPGateCoreTech()) { get(Protoss.DragoonRange) }

    //////////
    // Tech //
    //////////

    if (PvPRobo()) {
      get(Protoss.RoboticsFacility)
      if (shuttleFirst) {
        once(Protoss.Shuttle)
      }
      if (getObservers || getObservatory) {
        if (enemyDarkTemplarLikely && units(Protoss.Observer) == 0) {
          if (units(Protoss.Observatory) == 0) {
            if (gas < 100) {
              cancel(Protoss.RoboticsSupportBay, Protoss.ShuttleSpeed)
            }
          } else if (unitsComplete(Protoss.Observatory) > 0 && units(Protoss.Observer) == 0) {
            cancel(Protoss.Shuttle, Protoss.Reaver)
          }
        }
        get(RequestUnit(Protoss.Observatory, minStartFrameArg =
          With.units.ours
            .find(Protoss.Shuttle)
            .map(With.frame + _.remainingCompletionFrames - Protoss.Observatory.buildFrames)
            .getOrElse(if (shuttleFirst) Forever() else 0)))
        once(Protoss.Observer)
      } else {
        cancel(Protoss.Observer)
        if ( ! getObservatory) {
          cancel(Protoss.Observatory)
        }
      }
      if (getReavers) {
        val bayStartFrame = With.units.ours
          .find(Protoss.Observatory)
          .map(With.frame + _.remainingCompletionFrames - Protoss.RoboticsSupportBay.buildFrames)
          .getOrElse(
            if (getObservatory) Forever()
            else if (shuttleFirst) Maff.min(With.units.everOurs.filter(Protoss.Shuttle).map(_.completionFrame)).getOrElse(Forever()) - Protoss.RoboticsSupportBay.buildFrames
            else 0)
        get(RequestUnit(Protoss.RoboticsSupportBay, minStartFrameArg = bayStartFrame))
      }
      if (shuttleSpeed) {
        once(Protoss.Reaver)
        once(Protoss.ShuttleSpeed)
        once(Protoss.Shuttle)
      }
      trainRoboUnits()
      get(Protoss.DragoonRange)
      if (shouldExpand && (With.scouting.weControlOurNatural || unitsComplete(Protoss.Reaver) > 1)) {
        requireMiningBases(2)
      }
      trainGatewayUnits()
      get(2, Protoss.Gateway)
      get(Protoss.DragoonRange)
      get(3, Protoss.Gateway)

    } else if (PvPDT()) {
      // If we scout the mirror, just cannon expand
      if (With.fingerprints.dtRush() && (units(Protoss.TemplarArchives) == 0 || enemies(Protoss.Forge, Protoss.PhotonCannon) > 0)) {
        requireMiningBases(2)
      } else if ( ! enemyHasShown(Protoss.Observer, Protoss.Observatory)) {
        sneakyCitadel()
        get(Protoss.TemplarArchives)
        once(Math.min(2, unitsComplete(Protoss.Gateway)), Protoss.DarkTemplar)
      }
      if (unitsComplete(Protoss.Gateway) < 2) {
        trainGatewayUnits()
      }
      if (shouldExpand) { requireMiningBases(2) }
      pumpWorkers(oversaturate = true)
      trainGatewayUnits()
      get(2, Protoss.Gateway)
      requireMiningBases(2)

    } else if (PvPCoreExpand()) {
      if (shouldExpand) {
        requireMiningBases(2)
      }
      trainGatewayUnits()
      once(3, Protoss.Dragoon)
      requireMiningBases(2)
      get(3, Protoss.Gateway)

    // 3/4-Gate Goon
    } else {
      if (shouldExpand) {
        if (PvP4GateGoon()) {
          // PvPIdeas should be telling us when we need this anyway, but until it does so correctly, this is the workaround.
          buildCannonsAtNatural(1)
        }
        requireMiningBases(2)
      }
      once(2, Protoss.Dragoon)
      get(if (PvP3GateGoon()) 3 else 4, Protoss.Gateway)
      trainGatewayUnits()
      if (PvP3GateGoon()) { requireMiningBases(2) }
    }

    get(Protoss.DragoonRange)
    pumpWorkers(oversaturate = true)
    get(4, Protoss.Gateway)
    requireMiningBases(2)
  }

  private def trainRoboUnits(): Unit = {
    if (getObservers) {
      once(Protoss.Observer)
      if (With.fingerprints.dtRush()) pump(Protoss.Observer, 2)
    }
    if (units(Protoss.Reaver) >= 3) pumpShuttleAndReavers() else pump(Protoss.Reaver)
  }

  private def trainGatewayUnits(): Unit = {
    if (zAfterCore && zBeforeCore) once(2, Protoss.Zealot)
    else if (zAfterCore || zBeforeCore) once(Protoss.Zealot)
    once(Protoss.Dragoon)
    pump(Protoss.Dragoon)
    if ( ! PvPCoreExpand() || gas < 32) {
      pump(Protoss.Zealot)
    }
  }
}