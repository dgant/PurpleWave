package Planning.Plans.GamePlans.Protoss.PvP

import Debugging.SimpleString
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

  // Some build references:
  // CoreZ  Robo    Range   Speed Shuttle Double Reaver (2p Ramped):    https://youtu.be/5kvNatT3PuU?t=3340
  // NZCore Range   Gate    Robo  Shuttle Reaver        (4p Inverted):  https://youtu.be/5kvNatT3PuU?t=4400
  // NZCore Range   Gate    Robo  Shuttle Reaver        (4p Inverted):  https://youtu.be/dCn9WWIk4so?t=4658 (Same build as above) Shuttle > Reaver > Observatory > Reaver > Expand > Obs
  // NZCore Range   Citadel                             (4p Inverted):  https://youtu.be/5kvNatT3PuU?t=4423
  // CoreZ  Range   Robo    Gate  Obs     Gate          (4p Inverted):  https://youtu.be/dCn9WWIk4so?t=4658 (After seeing Robo) Shuttle > Reaver > Reaver > All-In
  // ZCoreZ Range   Citadel Gate  Archives              (2p Ramped):    https://youtu.be/dCn9WWIk4so?t=6884 Gate Robo
  // CoreZ  Range   2Goon   Nexus Robo    GateGate      (2p Ramped):    https://youtu.be/dCn9WWIk4so?t=6884 Obs

  var complete            : Boolean = false
  var atEarlyTiming       : Boolean = false
  var atMainTiming        : Boolean = false
  var earlyTimingClosed   : Boolean = false
  var mainTimingClosed    : Boolean = false
  var timingVs2Gate       : Boolean = false
  var timingVsProxy       : Boolean = false
  var shouldExpand        : Boolean = false
  var shouldAttack        : Boolean = false
  var shouldHarass        : Boolean = false
  var shouldHoldNatural   : Boolean = false
  var had2GateBeforeCore  : Boolean = false
  var hadTechBeforeRange  : Boolean = false
  var rangeDelayed        : Boolean = false
  
  // 10-12
  var commitZealots       : Boolean = false
  var sevenZealot         : Boolean = false
  // 1 Gate Core
  var zBeforeCore         : Boolean = false
  var zAfterCore          : Boolean = false
  // Robo
  var getObservers        : Boolean = false
  var getObservatory      : Boolean = false
  var getReavers          : Boolean = false
  var reaverAllIn         : Boolean = false
  var shuttleFirst        : Boolean = false
  var shuttleSpeed        : Boolean = false
  var doSneakyRobo        : Boolean = true
  // DT
  var greedyDT            : Boolean = false
  var cannonExpand        : Boolean = false

  trait CoreSequence  extends SimpleString
  object GCRange      extends CoreSequence
  object GCTech       extends CoreSequence
  object GCGate       extends CoreSequence
  object GGC          extends CoreSequence
  def gcrange : Boolean       = sequence == GCRange
  def gctech  : Boolean       = sequence == GCTech
  def gcgate  : Boolean       = sequence == GCGate
  def ggcore  : Boolean       = sequence == GGC
  var sequence: CoreSequence  = _

  override def activated: Boolean = true
  override def completed: Boolean = {
    complete ||= bases > 1
    complete &&= ! PvPDT() || cannonExpand || With.units.everOurs.exists(u => u.isOurs && u.complete && Protoss.DarkTemplar(u))
    complete
  }

  private def sneakyRobo(): Unit = {
    doSneakyRobo &&= units(Protoss.Probe) < 17 || scoutCleared
    if (doSneakyRobo) {
      status("SneakyRobo")
      get(Protoss.RoboticsFacility)
    } else {
      ?(With.strategy.isRamped,
        get(2, Protoss.Gateway),
        get(Protoss.DragoonRange))
    }
  }

  override def executeBuild(): Unit = {
    if (sequence == null) {
      sequence = if (PvP1012()) GGC else {
        var wTech   =   1.0
        var wGate   =   1.0
        wTech       *=  (With.geography.startLocations.length - 1)
        wTech       *=  Maff.or1(0,     With.strategy.isFlat || With.strategy.isInverted)
        wTech       *=  Maff.or1(0,     PvP3GateGoon() || PvP4GateGoon() ||  PvPGateCore())
        wGate       *=  Maff.or1(2,     enemyRecentStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99))
        wGate       *=  Maff.or1(1000,  enemyRecentStrategy(With.fingerprints.twoGate))
        wGate       *=  Maff.or1(.0001, enemyRecentStrategy(With.fingerprints.oneGateCore))
        Maff.sampleWeightedValues(
          (GCRange, 1.0),
          (GCTech,  wTech),
          (GCGate,  wGate)).getOrElse(GCRange)
      }
    }

    PvPGateCore() // Make sure we activate it if selected

    /////////////////////
    // Update strategy //
    /////////////////////

    // Swap into 2-Gate
    if ( ! have(Protoss.Assimilator) && With.frame < Minutes(2)()) {
      if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.gasSteal, With.fingerprints.mannerPylon) && ! With.fingerprints.cannonRush()) {
        sequence = GGC
        PvP1012.swapIn()
      }
    }
    if (PvP1012()) {
      if ( ! have(Protoss.Assimilator)) {
        PvP3Zealot()
        PvP5Zealot()
        if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99, With.fingerprints.nexusFirst, With.fingerprints.gasSteal)) {
          PvP5Zealot.swapIn()
          PvP3Zealot.swapOut()
        }
      }
      if ( ! haveComplete(Protoss.CyberneticsCore)) {
        sevenZealot = PvP5Zealot()
        sevenZealot &&= enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate, With.fingerprints.nexusFirst)
      }
    } else {
      if ( ! have(Protoss.CyberneticsCore)) {
        zBeforeCore = enemyRecentStrategy(With.fingerprints.twoGate99, With.fingerprints.proxyGateway, With.fingerprints.mannerPylon, With.fingerprints.gasSteal, With.fingerprints.cannonRush)
        zBeforeCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.oneGateCore)
        zBeforeCore ||= With.fingerprints.twoGate()
        zBeforeCore ||= gcgate
      }
      if ( ! haveComplete(Protoss.CyberneticsCore)) {
        zAfterCore = true
        zAfterCore &&= ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.coreBeforeZ)
        zAfterCore ||= enemyStrategy(With.fingerprints.mannerPylon, With.fingerprints.gasSteal, With.fingerprints.cannonRush)
        zAfterCore ||= gcgate || gctech || PvPDT()
      }
      if (units(Protoss.Gateway) < 2 && ! have(Protoss.RoboticsFacility, Protoss.CitadelOfAdun) && ! anyUpgradeStarted(Protoss.DragoonRange, Protoss.AirDamage)) {
        if (With.fingerprints.twoGate() || With.fingerprints.proxyGateway() || With.fingerprints.nexusFirst()) {
          sequence = GCGate
        } else if (
          With.fingerprints.cannonRush()
          || (With.fingerprints.earlyForge() && With.fingerprints.cannonRush.recently)
          || (With.fingerprints.rampBlock() && roll("ReactToRampBlock", 0.6))) {
          sequence = GCTech
          PvPRobo.swapIn()
          PvPReaver.swapIn()
          PvPDT.swapOut()
          PvP3GateGoon.swapOut()
          PvP4GateGoon.swapOut()
          PvPCoreExpand.swapOut()
        } else if (gcgate && enemyStrategy(With.fingerprints.oneGateCore) && ! With.strategy.isInverted) {
          sequence = ?(With.strategy.isRamped && (PvPRobo() || PvPDT()), GCTech, GCRange)
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
      && ! have(Protoss.Gateway)
      && ! enemyRecentStrategy(With.fingerprints.proxyGateway)
      && Stream(Arcadia, Aztec, Benzene, Longinus, MatchPoint, Heartbreak, Roadkill).exists(_())) {
      PvP1012.swapOut()
      PvP3Zealot.swapOut()
      PvP5Zealot.swapOut()
      sequence = ?(Stream(Heartbreak, Roadkill).exists(_()) || roll("1012ToGateCoreGate", 0.35), GCGate, GCRange)
    }
    // Goon+Obs is the strongest punishment against badly hidden DT openers.
    // A glimpse of Citadel doesn't sufficiently justify switching into Obs for its own sake,
    // as the Citadel could be a fake and the investment is a lot less than making even one Observer,
    // but a switch into full-blown Robotics at least lets us benefit from the investment if the Citadel was a fake
    if (employing(PvPCoreExpand, PvP3GateGoon, PvP4GateGoon) && enemiesHave(Protoss.CitadelOfAdun) && units(Protoss.Gateway) < 3) {
      if (roll("SwapGateIntoRoboVsCitadel", 0.3)) {
        PvPCoreExpand.swapOut()
        PvP3GateGoon.swapOut()
        PvP4GateGoon.swapOut()
        PvPRobo.swapIn()
        PvPObs.swapIn()
      }
    }
    // Robo is a very middle-of-the-road build, and has a few pointed weaknesses.
    // It's good against opponents playing diverse strategies but unimpressive against one-dimensional opponents.
    if (PvPRobo()
      && ! haveComplete(Protoss.CyberneticsCore)
      && ! have(Protoss.RoboticsFacility)
      && ! enemiesHave(Protoss.Forge, Protoss.CitadelOfAdun)) {

      // Core expand is advantaged against most Robo variants.
      // But we don't want to make this switch too predictably, as it's abusable.
      if ( ! With.strategy.isFixedOpponent
        && With.fingerprints.robo.recently
        && (With.fingerprints.robo() || ! With.fingerprints.dtRush.recently)
        && roll("SwapRoboIntoExpand", ?(With.fingerprints.robo(), 0.6, 0.25))) {
        PvPRobo.swapOut()
        PvPObs.swapOut()
        PvPReaver.swapOut()
        ?(units(Protoss.Gateway) > 1, PvP3GateGoon, PvPCoreExpand).swapIn()
      }
    }
    // If we catch them going Robo or Forge against our DT, abandon ship
    lazy val earlyForge = enemyStrategy(With.fingerprints.earlyForge, With.fingerprints.forgeFe, With.fingerprints.gatewayFe)
    if (PvPDT() && ! haveComplete(Protoss.TemplarArchives) && (enemyRobo || earlyForge)) {
      PvPDT.swapOut()
      if (With.fingerprints.dtRush() && ! PvPIdeas.preferObserverForDetection && roll("CannonExpand", 0.75)) {
        cannonExpand = true
      } else if (earlyForge && roll("SwapDTIntoRobo", 0.6)) {
        PvPRobo.swapIn()
        PvPReaver.swapIn()
      } else if (roll("SwapDTIntoExpand", 0.6)) {
        PvPCoreExpand.swapIn()
      } else if (roll("SwapDTInto4Gate", 0.3)) {
        PvP4GateGoon.swapIn()
      } else {
        PvP3GateGoon.swapIn()
      }
    }
    // Consider swapping out of DT if we've been caught, or if we scout them going DT first
    if (PvPDT() && scoutCleared && ! haveComplete(Protoss.Dragoon)) {
      val caught            = With.units.ours.filter(u => u.isAny(Protoss.CitadelOfAdun, Protoss.TemplarArchives) && u.knownToOpponents && ! u.visibleToOpponents).toVector
      val archivesComplete  = caught.exists(c => Protoss.TemplarArchives(c) &&    c.complete)
      val archivesStarted   = caught.exists(c => Protoss.TemplarArchives(c) &&  ! c.complete)
      val citadelComplete   = caught.exists(c => Protoss.CitadelOfAdun(c)   &&    c.complete)
      val maybeMirror       = (enemyDarkTemplarLikely || enemyRecentStrategy(With.fingerprints.dtRush)) &&  ! With.fingerprints.robo()
      val mirrorMultiplier  = ?(maybeMirror, 0.25, 1.0)
      var swapOutOfDT       = false
      if (archivesComplete) {
        // We've come too far; no point switching
      } else if (archivesStarted) {
        swapOutOfDT = roll("DTSwap", 0.65 * mirrorMultiplier)
      } else if (citadelComplete) {
        swapOutOfDT = roll("DTSwap", 0.75 * mirrorMultiplier)
      } else if (caught.nonEmpty) {
        swapOutOfDT = roll("DTSwap", 0.85 * mirrorMultiplier)
      } else if (maybeMirror && enemyDarkTemplarLikely && unitsComplete(Protoss.CitadelOfAdun) < 0 && ! have(Protoss.Forge)) {
        swapOutOfDT = roll("DTSwap", 0.75)
      }
      if (swapOutOfDT) {
        PvPDT.swapOut()
        if (maybeMirror && roll("DTToRobo", 0.6)) {
          PvPRobo.swapIn()
          PvPObs.swapIn()
        } else if ( ! citadelComplete && roll("DTToFE", 0.6)) {
          PvPCoreExpand.swapIn()
        } else if (roll("DTTo4Gate", 0.4)) {
          PvP4GateGoon.swapIn()
        } else {
          PvP3GateGoon.swapIn()
        }
      }
    }
    if ( ! PvPDT()) {
      cancel(Protoss.CitadelOfAdun, Protoss.TemplarArchives)
      if (enemiesHave(Protoss.Observer, Protoss.Observatory)) {
        cancel(Protoss.DarkTemplar)
      }
    }

    /////////////////////////////
    // Tech-specific decisions //
    /////////////////////////////

    if (PvPRobo()) {
      val enemyCitadel  = enemyDarkTemplarLikely || enemiesHave(Protoss.CitadelOfAdun)
      // TODO: Also all-in vs. 1 Gate Core Expand
      reaverAllIn     ||= enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.dtRush)
      reaverAllIn     ||= With.fingerprints.twoGate() && enemyBases > 1
      reaverAllIn     ||= With.fingerprints.twoGate() && (enemyHasShown(Protoss.CitadelOfAdun) || enemyHasUpgrade(Protoss.ZealotSpeed))
      getReavers        = PvPReaver()
      getReavers      ||= reaverAllIn
      getReavers      ||= have(Protoss.RoboticsSupportBay, Protoss.Reaver, Protoss.Shuttle)
      getReavers      ||= enemyStrategy(With.fingerprints.fourGateGoon)

      // Look for reasons to avoid making an Observer.
      // It's okay to switch even if we already started an Observatory or Observers,
      // because we can cancel and switch out of them at any time.

      getObservatory    = true
      getObservers      = true
      if (With.fingerprints.dtRush()) { // Yep, get those Observers
      } else if (shuttleSpeed) { // This strategy demands a ton of gas; we can't afford the Observer
        getObservatory  = false
        getObservers    = false
      } else if (enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) { // We have all the intel we need to act, and can rule out DT
        getObservatory  = false
        getObservers    = false
      } else if (PvPObs()) { // Obs is what we're here for!
      } else if ( ! With.fingerprints.dragoonRange() && With.units.enemy.exists(e => Protoss.Dragoon(e) && e.lastSeen > GameTime(5, 15)())) { // If Dragoon range is supiciously absent we should prepare for DT
      } else if (enemyRecentStrategy(With.fingerprints.dtRush)) { // Don't overthink it
      } else {
        var p = 1.0
        p *= ?(enemyRecentStrategy(With.fingerprints.fourGateGoon, With.fingerprints.threeGateGoon, With.fingerprints.robo),  0.6, 1.0)
        p *= ?(trackRecordLacks(With.fingerprints.dtRush),                                                                    0.6, 1.0)
        getObservatory  =                   roll("SpeculativeObservatory",  p)
        getObservers    = getObservatory && roll("SpeculativeObservers",   p) // So the probability of obs is the *joint* probability
      }
      val dtSmell = enemyCitadel || enemyHasShown(Protoss.Forge, Protoss.PhotonCannon) || With.sense.netDamage > 2 * Protoss.Dragoon.subjectiveValue
      getObservatory  ||= dtSmell
      getObservers    ||= dtSmell
      getReavers      ||= ! getObservatory && ! have(Protoss.Observatory)
      if ( ! have(Protoss.Shuttle, Protoss.RoboticsSupportBay, Protoss.Observatory)) {
        shuttleFirst = getReavers && ! enemyCitadel
      }
      shuttleSpeed = shuttleFirst && gctech && ! getObservatory && ! getObservers && ! have(Protoss.Observatory, Protoss.Observer) && roll("ShuttleSpeedRush", 0.0)
    } else if (PvPDT()) {
      greedyDT    = have(Protoss.TemplarArchives)
      greedyDT  &&= ! enemyStrategy(With.fingerprints.twoGate, With.fingerprints.dtRush)
      greedyDT  &&= roll("DTGreedyExpand", ?(enemyRecentStrategy(With.fingerprints.dtRush), 0.0, 0.5))
    }

    had2GateBeforeCore  ||= units(Protoss.Gateway) >= 2 && ! have(Protoss.CyberneticsCore)
    hadTechBeforeRange  ||= have(Protoss.RoboticsFacility, Protoss.CitadelOfAdun) && ! upgradeStarted(Protoss.DragoonRange)
    rangeDelayed        =   ! Protoss.DragoonRange() && (Maff.fromBoolean(With.fingerprints.twoGate() || With.fingerprints.nexusFirst()) - Maff.fromBoolean(had2GateBeforeCore) - Maff.fromBoolean(hadTechBeforeRange) > 0) // TODO: Count enemy tech before range
    timingVs2Gate       ||= With.fingerprints.twoGate() && ! enemyHasShown(Protoss.Dragoon) && ! had2GateBeforeCore && haveComplete(Protoss.Dragoon)
    timingVsProxy       ||= With.fingerprints.proxyGateway() && unitsComplete(IsWarrior) > 5
    // Identify when we reach an attack timing
    atEarlyTiming ||= PvP1012()         && unitsComplete(Protoss.Zealot)  >= 3
    atEarlyTiming ||= gcgate            && unitsComplete(Protoss.Dragoon) > enemies(Protoss.Dragoon)
    atEarlyTiming ||=                      unitsComplete(Protoss.Dragoon) > 0 && With.fingerprints.proxyGateway()
    atEarlyTiming ||= PvPDT()           && upgradeComplete(Protoss.DragoonRange, Seconds(30)()) && ! enemyStrategy(With.fingerprints.dragoonRange, With.fingerprints.threeGateGoon, With.fingerprints.robo) // Attack if we're not clear on their plan to suss out DT mirror
    atMainTiming  ||= PvP3GateGoon()    && upgradeComplete(Protoss.DragoonRange)  && unitsCompleteFor(Protoss.Dragoon.buildFrames, Protoss.Gateway) >= 3 && unitsComplete(IsWarrior) >= 6
    atMainTiming  ||= PvP4GateGoon()    && upgradeComplete(Protoss.DragoonRange)  && unitsCompleteFor(Protoss.Dragoon.buildFrames, Protoss.Gateway) >= 4 && unitsComplete(IsWarrior) >= 7
    atMainTiming  ||= PvPRobo()         && upgradeComplete(Protoss.DragoonRange)  && unitsComplete(Protoss.Reaver) * unitsComplete(Protoss.Shuttle) >= 2 && unitsComplete(IsWarrior) >= 8
    atMainTiming  ||= PvPDT()                                                     && haveComplete(Protoss.DarkTemplar)
    atMainTiming  ||= haveComplete(Protoss.Observer)  && With.fingerprints.dtRush()
    atMainTiming  ||= haveComplete(Protoss.Reaver)    && With.fingerprints.cannonRush()
    atMainTiming  ||= enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.cannonRush, With.fingerprints.nexusFirst, With.fingerprints.rampBlock)
    atMainTiming  ||= enemyBases > 1
    atMainTiming  ||= With.fingerprints.cannonRush.recently && ! With.fingerprints.gatewayFirst()
    // There may not be a timing depending on what our opponent does,
    // or the timing window might close permanently.
    earlyTimingClosed   ||= PvP1012()         && With.fingerprints.twoGate()
    earlyTimingClosed   ||= PvP1012()         && (enemyHasUpgrade(Protoss.DragoonRange) || With.fingerprints.oneGateCore()) && With.frame > GameTime(5, 10)() && ! upgradeComplete(Protoss.DragoonRange)
    earlyTimingClosed   ||= gcgate            && enemyHasUpgrade(Protoss.DragoonRange) && ! safePushing && enemyStrategy(With.fingerprints.twoGateGoon, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)
    mainTimingClosed    ||= PvP3GateGoon()    && enemyStrategy(With.fingerprints.fourGateGoon, With.fingerprints.threeGateGoon)
    mainTimingClosed    ||= PvP4GateGoon()    && With.fingerprints.fourGateGoon()
    mainTimingClosed    ||= PvPDT()           && enemiesHaveComplete(Protoss.Observer, Protoss.PhotonCannon)
    shouldAttack          = atEarlyTiming  && ! earlyTimingClosed
    shouldAttack        ||= atMainTiming   && ! mainTimingClosed
    shouldAttack        ||= timingVsProxy
    shouldAttack        &&= safePushing
    shouldAttack        ||= timingVs2Gate
    shouldAttack        &&= ! enemiesHave(Protoss.DarkTemplar) || unitsComplete(Protoss.Observer) > 1
    shouldAttack        ||= With.units.ours.exists(_.agent.commit) && With.frame < Minutes(5)() // Ensure that committed Zealots keep wanting to attack
    shouldHarass          = upgradeStarted(Protoss.ShuttleSpeed) && unitsComplete(Protoss.Reaver) > 1

    if (reaverAllIn) {
      shouldExpand = units(Protoss.Reaver) >= 2 && units(Protoss.Shuttle) >= 1
    } else if (PvPRobo()) {
      shouldExpand    = haveComplete(Protoss.Reaver)
      shouldExpand  ||= ! getReavers && unitsComplete(IsWarrior) >= ?(enemyBases > 1, 6, 12)
      shouldExpand  ||= enemyRobo
      shouldExpand  &&= PvPIdeas.pvpSafeToMoveOut
      shouldExpand  &&= ! shuttleSpeed
      shouldExpand  &&= Protoss.DragoonRange()
      shouldExpand  ||= unitsComplete(Protoss.Reaver) > 1
    } else if (PvPDT()) {
      shouldExpand    = atMainTiming
      shouldExpand  ||= have(Protoss.DarkTemplar) && (PvPIdeas.pvpSafeAtHome || With.scouting.enemyProximity < 0.75)
      shouldExpand  ||= greedyDT
    } else if (PvP3GateGoon()) {
      shouldExpand    = atMainTiming
      shouldExpand  &&= units(IsWarrior) >= 9
      shouldExpand  &&= PvPIdeas.pvpSafeAtHome
    } else if (PvP4GateGoon()) {
      shouldExpand    = atMainTiming && ! mainTimingClosed && unitsEver(IsWarrior) >= 20
      shouldExpand  ||= unitsComplete(IsWarrior) >= ?(safePushing, ?(PvPIdeas.enemyContained, 14, 20), 28)
    }
    shouldExpand  ||= enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe) && ! With.fingerprints.cannonRush()
    shouldExpand  ||= unitsComplete(IsWarrior) >= 30 // We will get contained if we wait too long

    // If we want to expand, make sure we control our natural
    shouldHoldNatural   = safeDefending
    shouldHoldNatural &&= enemyBases > 1 || ! PvPDT()    || unitsComplete(Protoss.DarkTemplar) > 0
    shouldHoldNatural &&= enemyBases > 1 || ! getReavers || unitsComplete(Protoss.Reaver) > 0
    shouldHoldNatural &&= ! rangeDelayed || Protoss.DragoonRange()
    shouldHoldNatural &&= enemyHasShown(Protoss.Dragoon)
    shouldHoldNatural ||= shouldAttack
    shouldHoldNatural ||= shouldExpand && bases < 2

    // Chill vs. 2-Gate until we're ready to defend
    if ( ! PvP1012() && With.fingerprints.twoGate() && unitsEver(IsAll(Protoss.Dragoon, IsComplete)) == 0) {
      aggression(0.6)
    } else if (atMainTiming) {
      // Amp up aggression to ensure we can get down our ramp
      aggression(1.0 + 0.25 * unitsComplete(Protoss.Reaver))
    }

    /////////////
    // Logging //
    /////////////

    if ( ! ggcore) {
      status(
        ?(zBeforeCore,
          ?(zAfterCore, "ZCoreZ", "ZCore"),
          ?(zAfterCore, "CoreZ",  "NZCore")))
      status(sequence.toString)
    }
    status(sevenZealot,       "SevenZealots")
    status(commitZealots,     "CommitZealots")
    status(shuttleFirst,      "ShuttleFirst")
    status(shuttleSpeed,      "ShuttleSpeed")
    status(getObservers,      "Obs")
    status(getObservatory,    "Observatory")
    status(getReavers,        "Reaver")
    status(reaverAllIn,       "ReaverAllIn")
    status(greedyDT,          "GreedyDT")
    status(atEarlyTiming,     "Timing1")
    status(earlyTimingClosed, "Timing1Done")
    status(atMainTiming,      "Timing2")
    status(mainTimingClosed,  "Timing2Done")
    status(timingVs2Gate,     "TimingVs2Gate")
    status(timingVsProxy,     "TimingVsProxy")
    status(shouldAttack,      "Attack")
    status(shouldHarass,      "Harass")
    status(shouldExpand,      "Expand")
    status(shouldHoldNatural, "HoldNat")

    if (shouldAttack) { attack() }
    if (shouldHarass) { harass() }
    if (shouldHoldNatural) { holdFoyer() }
    PvPIdeas.monitorSafely()

    //////////////
    // Scouting //
    //////////////

    if ( ! enemiesHave(Protoss.Dragoon)
      && ! With.fingerprints.proxyGateway()
      && ! With.units.enemy.filter(Protoss.CyberneticsCore).exists(With.frame - _.completionFrame > Protoss.Dragoon.buildFrames - Seconds(5)())) {
      if (PvP1012()) {
        if ( ! foundEnemyBase) {
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
        commitZealots = false
      } else if (frame < GameTime(4, 15)() && ! enemiesHaveComplete(Protoss.PhotonCannon)) {
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

    //////////////////////
    // React against DT //
    //////////////////////

    if ( ! greedyDT) {
      PvPIdeas.requireTimelyDetection()
    }

    ///////////////////////
    // React against 9-9 //
    ///////////////////////

    if (enemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.twoGate99)
      && With.frame < Minutes(7)()
      && unitsComplete(IsWarrior) < 7
      // If they just make a few Zealots and stop, we don't want to overreact and die to, say, a DT swap
      && ! enemiesHave(Protoss.CyberneticsCore, Protoss.Dragoon)) {
      status("99Defense")
      gasLimitCeiling(200)
      if (units(Protoss.Gateway) < 2 || unitsComplete(Protoss.Probe) < 15) {
        cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.DragoonRange, Protoss.AirDamage)
        gasWorkerCeiling(0)
      } else if ( ! have(Protoss.CyberneticsCore)) {
        gasWorkerCeiling(0)
      } else if ( ! haveComplete(Protoss.CyberneticsCore)) {
        if ( ! have(Protoss.Dragoon)) {
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
      get(Protoss.ShieldBattery)
      if (With.units.enemy.exists(u => Protoss.Gateway(u) && u.proximity > 0.7)) {
        get(3, Protoss.Gateway)
      }
      get(Protoss.Assimilator)
      get(Protoss.CyberneticsCore)
      get(3, Protoss.Gateway)
      get(Protoss.DragoonRange)
    }

    //////////////////////
    // React against FE //
    //////////////////////

    val shouldFESafe    = shouldExpand && (With.fingerprints.forgeFe() || With.fingerprints.gatewayFe()) && (enemiesHave(Protoss.PhotonCannon) || enemyBases >= 2)
    val shouldFEGreedy  = shouldFESafe && With.fingerprints.forgeFe() && (enemies(Protoss.PhotonCannon) > 1 || enemyBases >= 2)
    val shouldFE        = shouldFESafe || shouldFEGreedy
    if (shouldFE && unitsEver(Protoss.Nexus) < 2 && minerals < 300) {
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.DragoonRange)
    }
    if (shouldFEGreedy) {
      get(15, Protoss.Probe)
      get(2, Protoss.Nexus)
      get(2, Protoss.Pylon)
      get(16, Protoss.Probe)
      get(Protoss.Assimilator)
      get(17, Protoss.Probe)
      get(Protoss.CyberneticsCore)
    } else if (shouldFESafe) {
      get(12, Protoss.Probe)
      get(2, Protoss.Pylon)
      get(13, Protoss.Probe)
      get(Protoss.Zealot)
      get(15, Protoss.Probe)
      get(2, Protoss.Zealot)
      get(17, Protoss.Probe)
      get(2, Protoss.Nexus)
      get(18, Protoss.Probe)
      get(3, Protoss.Zealot)
      get(19, Protoss.Probe)
      get(Protoss.Assimilator)
      get(20, Protoss.Probe)
      get(Protoss.CyberneticsCore)
    }

    ////////////
    // 2-Gate //
    ////////////

    else if (PvP1012()) { // https://liquipedia.net/starcraft/2_Gate_(vs._Protoss)
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
          if (gcgate) { // https://liquipedia.net/starcraft/2_Gate_Reaver_(vs._Protoss
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

          if (gctech) {
            once(17, Protoss.Probe)
            if      (PvPDT())   sneakyCitadel()
            else if (PvPRobo()) sneakyRobo()
            else                get(Protoss.DragoonRange)
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
            if (gcgate) {
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
    if (gcgate || PvP1012()) { once(2, Protoss.Gateway) }
    once(Protoss.Dragoon)
    if ( ! gctech) { get(Protoss.DragoonRange) }

    //////////
    // Tech //
    //////////

    if (PvPRobo()) {
      get(Protoss.RoboticsFacility)
      if (shuttleFirst) {
        once(Protoss.Shuttle)
      }
      if (getObservers || getObservatory) {
        if (enemyDarkTemplarLikely && ! have(Protoss.Observer)) {
          if ( ! have(Protoss.Observatory)) {
            if (gas < 100) {
              cancel(Protoss.RoboticsSupportBay, Protoss.ShuttleSpeed)
            }
          } else if (haveComplete(Protoss.Observatory) && ! have(Protoss.Observer)) {
            // TODO: Could cancel a little sooner if Observatory is in progress
            cancel(Protoss.Shuttle, Protoss.Reaver)
          }
        }
        get(RequestUnit(Protoss.Observatory, minStartFrameArg =
          With.units.ours
            .find(Protoss.Shuttle)
            .map(With.frame + _.remainingCompletionFrames - Protoss.Observatory.buildFrames)
            .getOrElse(?(shuttleFirst, Forever(), 0))))
        if (getObservers) {
          once(Protoss.Observer)
        }
      } else {
        cancel(Protoss.Observer)
        cancel(Protoss.Observatory)
      }
      if (getReavers) {
        var bayStartFrame = With.frame - Protoss.RoboticsSupportBay.buildFrames
        if (shuttleFirst && ! With.units.everOurs.exists(u => u.complete && Protoss.Shuttle(u))) {
          bayStartFrame += With.units.ours.find(Protoss.Shuttle).map(_.remainingCompletionFrames).getOrElse(Protoss.Shuttle.buildFrames)
        }
        if (getObservatory && getObservers) {
          bayStartFrame += With.units.ours.find(Protoss.Observatory).map(_.remainingCompletionFrames).getOrElse(Protoss.Observatory.buildFrames)
          if (! With.units.everOurs.exists(u => u.complete && Protoss.Observer(u))) {
            bayStartFrame += With.units.ours.find(Protoss.Observer).map(_.remainingCompletionFrames).getOrElse(Protoss.Observer.buildFrames)
          }
        }
        get(RequestUnit(Protoss.RoboticsSupportBay, minStartFrameArg = bayStartFrame))
      }
      if (getReavers) {
        once(Protoss.Reaver)
      }
      if (shuttleSpeed) {
        once(Protoss.ShuttleSpeed)
        once(Protoss.Shuttle)
      }
      get(Protoss.DragoonRange)
      if (shouldExpand && (With.scouting.weControlOurFoyer || units(Protoss.Reaver) > 1)) {
        expand()
      }
      trainRoboUnits()
      if (enemyStrategy(With.fingerprints.twoGateGoon, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)) {
        get(3, Protoss.Gateway)
      } else {
        pumpWorkers(oversaturate = true) // Need to keep worker production up in case they just expand
      }
      trainGatewayUnits()
      pumpWorkers(oversaturate = true)
      get(3, Protoss.Gateway)
    } else if (PvPDT()) {
      if (cannonExpand) {
        expand()
      } else if ( ! enemyHasShown(Protoss.Observer, Protoss.Observatory)) {
        sneakyCitadel()
        get(Protoss.TemplarArchives)
        once(Math.min(2, unitsComplete(Protoss.Gateway)), Protoss.DarkTemplar)
      }
      if (unitsComplete(Protoss.Gateway) < 2) {
        trainGatewayUnits()
      }
      if (shouldExpand) { expand() }
      pumpWorkers(oversaturate = true)
      trainGatewayUnits()
      get(2, Protoss.Gateway)
      expand()

    } else if (PvPCoreExpand()) {
      get(Protoss.DragoonRange)
      if (shouldExpand) {
        expand()
      }
      trainGatewayUnits()
      once(3, Protoss.Dragoon)
      expand()
      get(3, Protoss.Gateway)

    // 3/4-Gate Goon
    } else {
      get(Protoss.DragoonRange)
      if (With.scouting.ourProximity < 0.5 && safePushing && ! With.fingerprints.robo() && With.sense.netDamage > 400) {
        get(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      }
      if (shouldExpand) {
        pumpWorkers(oversaturate = true)
        if (PvP4GateGoon() && enemyBases < 2) {
          buildCannonsAtFoyer(1)
        }
        expand()
      }
      once(2, Protoss.Dragoon)
      get(?(PvP3GateGoon(), 3, 4), Protoss.Gateway)
      trainGatewayUnits()
      if (PvP3GateGoon()) { expand() }
    }

    get(Protoss.DragoonRange)
    pumpWorkers(oversaturate = true)
    get(4, Protoss.Gateway)
    expand()
  }

  private def trainRoboUnits(): Unit = {
    var pumpObs = getObservers
    pumpObs &&= ! With.fingerprints.fourGateGoon()
    pumpObs &&= ! enemyRobo
    pumpObs &&= enemyBases < 2
    pumpObs &&= ! reaverAllIn
    pumpObs &&= units(Protoss.Reaver) >= 2 * units(Protoss.Shuttle)
    pumpObs ||= With.fingerprints.dtRush()
    if (getObservers) once(Protoss.Observer)
    if (pumpObs) pump(Protoss.Observer, 2) else if (units(Protoss.Observer) > 1) cancel(Protoss.Observer)
    if (reaverAllIn || units(Protoss.Reaver) >= 2) pumpShuttleAndReavers() else pump(Protoss.Reaver)
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

  private def holdFoyer(): Unit = {
    With.blackboard.basesToHold.set(Vector(With.geography.ourFoyer))
  }

  private def expand(): Unit = {
    if ( ! With.scouting.enemyControls(With.geography.ourFoyer)) {
      requireMiningBases(2)
    }
  }
}