package Gameplans.Protoss.PvT

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Requests.RequestUnit
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._
import Utilities.?
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.{IsAll, IsProxied, IsWarrior, IsWorker}

abstract class PvTOpeners extends GameplanImperative {

  var openingComplete: Boolean = false

  override def doWorkers(): Unit = pumpWorkers(oversaturate = true, maximumTotal = 70)

  override def executeBuild(): Unit = {
    open()
  }

  def open(): Unit = {
    if (openingComplete) return
    doOpeningShortCircuits()
          if (PvT13Nexus())       open12NexusGateScout()
    else  if (PvTZealotExpand())  openZealotExpand()
    else  if (PvTZZCoreZ())       openZZCoreZ()
    else  if (PvTRangeless())     open20Rangeless()
    else  if (PvT28Nexus())       open28Nexus()
    else  if (PvT910())           open910()
    else  if (PvT1015())          open1015()
    else  if (PvT4Gate())         open4Gate()
    else  if (PvT1BaseReaver())   openReaver()
    else  if (PvT29Arbiter())     open29Arbiter()
    else  if (PvTDT())            openDT()
    else  if (PvTGasSteal())      openGasSteal()
    else                          openZZCoreZ() // A safe default if something went wrong
  }

  private def openGateCore(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(Protoss.Assimilator)
    once(13, Protoss.Probe)
    doOpeningReactions()
    once(Protoss.CyberneticsCore)
  }

  def open12NexusGateScout(): Unit = {
    // Reference: https://www.youtube.com/watch?v=AuIqTCxQ1PY
    // Flash says cross-scout; if we verify cross-spawn I believe the correct response is go direct to one gate core, maybe depending on gas.
    With.blackboard.crossScout.set(true)
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    doOpeningReactions()
    once(12, Protoss.Probe)
    once(2, Protoss.Nexus)
    once(14, Protoss.Probe)
    once(Protoss.Gateway)
    scoutOn(Protoss.Gateway)
    once(15, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(16, Protoss.Probe)
    once(Protoss.Assimilator)
    once(17, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(2, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(19, Protoss.Probe)
    once(2, Protoss.Dragoon)
    once(20, Protoss.Probe)
    get(Protoss.DragoonRange)
    once(21, Protoss.Probe)
    // Flash follows with robo, range, and fast arbiter, and thus takes 2nd gas here
    // TODO: I think one gate core if cross-spawn, two if close. Also might depend on gas/no gas
    // Then: Robo if rax expand; range if not
    openingComplete ||= units(Protoss.Nexus) > 1 && unitsEver(Protoss.Dragoon) > 1
  }

  def openZealotExpand(): Unit = {
    // References:
    // #1 https://youtu.be/jYRHZVAjhX8?t=4932
    // #2 https://www.youtube.com/watch?v=9uKU2hevkCQ
    // #3 https://discord.com/channels/828899288616140811/890891217636818964/915594910122516520
    // This build is the one in #2/#3. #1 delays Pylon and lets Nexus produce the supply, which gets you faster Dragoon but worse economy.
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    doOpeningReactions()
    once(14, Protoss.Probe)
    once(2, Protoss.Nexus)
    scoutOn(Protoss.Nexus, quantity = 2)
    once(Protoss.Assimilator)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(16, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(17, Protoss.Probe)
    once(Protoss.Dragoon)
    openingComplete ||= units(Protoss.Nexus) > 1 && units(Protoss.Dragoon) > 1
  }

  def openZZCoreZ(): Unit = {
    // Reference: https://www.youtube.com/watch?v=MXYRhJOmOkc
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    scoutOn(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    doOpeningReactions()
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
    once(20, Protoss.Probe)
    once(Protoss.DragoonRange)
    once(Protoss.Dragoon)
    once(21, Protoss.Probe)
    once(2, Protoss.Nexus)
    openingComplete ||= units(Protoss.Nexus) > 1 && unitsEver(Protoss.Dragoon) > 0
  }

  def open20Rangeless(): Unit = {
    // Reference: https://www.youtube.com/watch?v=gCtO31tlrBs
    openGateCore()
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    once(Protoss.Dragoon)
    once(18, Protoss.Probe)
    once(2, Protoss.Nexus)
    scoutOn(Protoss.Nexus, quantity = 2)
    once(2, Protoss.Dragoon)
    once(19, Protoss.Probe)
    if (gasCapsUntouched) {
      gasWorkerCeiling(Math.max(1, unitsComplete(Protoss.Probe) - 12))
    }
    openingComplete ||= units(Protoss.Nexus) > 1 && unitsEver(Protoss.Dragoon) > 1
  }

  def open28Nexus(): Unit = {
    // Reference: https://liquipedia.net/starcraft/28_Nexus_(vs._Terran)
    openGateCore()
    scoutOn(Protoss.CyberneticsCore) // I think if BBS is possibility you might want to cross-spawn
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    once(Protoss.Dragoon)
    once(17, Protoss.Probe)
    once(Protoss.DragoonRange)
    if (gasCapsUntouched && unitsEver(Protoss.Dragoon) > 0 && haveGasForUpgrade(Protoss.DragoonRange)) {
      gasWorkerCeiling(2)
    }
    once(19, Protoss.Probe)
    once(2, Protoss.Dragoon)
    once(20, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(21, Protoss.Probe)
    once(3, Protoss.Dragoon)
    once(2, Protoss.Nexus)
    once(22, Protoss.Probe)
    once(4, Protoss.Dragoon)
    openingComplete ||= unitsEver(Protoss.Dragoon) > 3
  }

  def open910(): Unit = {
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
    doOpeningReactions()
    once(14, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.Assimilator)
    once(17, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(19, Protoss.Probe)
    openingComplete ||= unitsEver(Protoss.Dragoon) > 1
  }

  def open1015(): Unit = {
    // No scout -- let the Dragoons do the work
    openGateCore()
    once(15, Protoss.Probe)
    once(2, Protoss.Gateway)
    // Liquipedia offers three variations; Namu offers a fourth. We prioritize range to hit bunkers ASAP
    once(Protoss.DragoonRange)
    once(1, Protoss.Dragoon)
    once(2, Protoss.Pylon)
    once(3, Protoss.Dragoon)
    once(3, Protoss.Pylon)
    once(5, Protoss.Dragoon)
    val targetNexi = ?(enemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac), 2, 3)
    once(targetNexi, Protoss.Nexus)
    openingComplete ||= units(Protoss.Nexus) >= targetNexi
  }

  def openReaver(): Unit = {
    openGateCore()
    once(14, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    scoutOn(Protoss.Probe, quantity = 16)
    once(Protoss.Dragoon)
    once(17, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(Protoss.RoboticsFacility)
    once(18, Protoss.Probe)
    once(2, Protoss.Dragoon)
    once(19, Protoss.Probe)
    once(3, Protoss.Dragoon)
    // Just spitballing this part; not refined
    once(Protoss.Shuttle)
    once(4, Protoss.Pylon)
    once(20, Protoss.Probe)
    once(Protoss.RoboticsSupportBay)
    once(21, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(Protoss.Reaver)
    once(2, Protoss.Nexus)
    once(5, Protoss.Pylon)
    openingComplete ||= units(Protoss.Nexus) > 1 && units(Protoss.Reaver) > 0
  }

  def open29Arbiter(): Unit = {
    // https://tl.net/forum/bw-strategy/538921-pvt-revolution-29-arbiter-build
    gasWorkerFloor(3)
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    once(Protoss.Assimilator)
    once(11, Protoss.Probe)
    once(Protoss.Gateway)
    once(14, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    scoutOn(Protoss.Pylon, 2)
    once(18, Protoss.Probe)
    once(Protoss.CitadelOfAdun)
    once(Protoss.Dragoon)
    once(19, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(21, Protoss.Probe)
    once(Protoss.Stargate, Protoss.TemplarArchives)
    once(24, Protoss.Probe)
    requireBases(2)
    once(27, Protoss.Probe)
    once(Protoss.ArbiterTribunal, Protoss.DarkTemplar)
    once(31, Protoss.Probe)
    once(Protoss.Arbiter)
    once(2, Protoss.Dragoon)
    openingComplete ||= unitsEver(Protoss.Arbiter) >= 1 && unitsEver(Protoss.Dragoon) >= 2
  }

  def openDT(): Unit = {
    openGateCore()
    scoutOn(Protoss.CyberneticsCore)
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    once(Protoss.Dragoon)
    once(17, Protoss.Probe)
    sneakyCitadel()
    once(19, Protoss.Probe)
    once(2, Protoss.Dragoon)
    once(20, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(21, Protoss.Probe)
    once(Protoss.TemplarArchives)
    once(3, Protoss.Dragoon)
    once(2, Protoss.Nexus)
    once(Protoss.DarkTemplar)
    openingComplete ||= units(Protoss.Nexus) > 1 && units(Protoss.DarkTemplar) > 0
  }

  def openGasSteal(): Unit = {
    // Rain vs. Sharp: https://youtu.be/IckWDksioQE?t=1293
    // 10 Gas Steal 11 Gate
    // Mini vs. Organ https://youtu.be/Cl2MHjmGBLk?t=1342
    // 10 Gate 12 Gas Steal
    val buildInNatural = With.placement.wall.isDefined && With.frame < GameTime(2, 0)()

    scoutOn(Protoss.Pylon)

    once(8, Protoss.Probe)
    if (buildInNatural) {
      get(1,
        Protoss.Pylon,
        new PlacementQuery(Protoss.Pylon)
          .preferTile()
          .preferLabelNo()
          .preferLabelYes(PlaceLabels.Wall)
          .preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*))
    } else {
      once(Protoss.Pylon)
    }

    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(11, Protoss.Probe)

    val scout = With.tactics.workerScout.lock.units.headOption
    val enemyMain = With.scouting.enemyMain
    if (scout.isDefined && enemyMain.exists(_.gas.forall(_.isNeutral)) && With.frame < GameTime(2, 5)()) {
      get(RequestUnit(Protoss.Assimilator, 1, placementQueryArg = Some(new PlacementQuery(Protoss.Assimilator).requireBase(enemyMain.get))))
    }
    if (With.fingerprints.wallIn() && safeDefending && With.frame < Minutes(4)()) {
      cancel(Protoss.Zealot)
    }

    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)

    get(RequestUnit(Protoss.Assimilator, 1, placementQueryArg = Some(new PlacementQuery(Protoss.Assimilator).requireBase(With.geography.ourMain))))
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(20, Protoss.Probe)
    once(Protoss.DragoonRange)
    once(Protoss.Dragoon)
    once(4, Protoss.Gateway)
    once(22, Protoss.Probe)
    once(10, Protoss.Dragoon)
    if (unitsEver(Protoss.Gateway) < 4) {
      gasWorkerCeiling(2)
    }
  }

  def open4Gate(): Unit = {
    openGateCore()
    // No scout; need minerals and are hitting a later timing
    once(15, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(16, Protoss.Probe)
    once(Protoss.Dragoon)
    once(17, Protoss.Probe)
    once(Protoss.DragoonRange)
    // TODO: only 2 on gas here
    once(19, Protoss.Probe)
    once(2, Protoss.Dragoon)
    once(20, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(21, Protoss.Probe)
    once(4, Protoss.Gateway)
    once(3, Protoss.Dragoon)
    once(4, Protoss.Pylon)
    once(7, Protoss.Dragoon)
    once(5, Protoss.Pylon)
    once(11, Protoss.Dragoon)
    openingComplete ||= unitsEver(Protoss.Dragoon) >= 11
  }

  def doOpeningShortCircuits(): Unit = {
    if (PvT28Nexus() && enemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE)) {
      status("AcceleratedExpand")
      requireMiningBases(2)
    }
    if (PvTZZCoreZ() && enemyStrategy(With.fingerprints.wallIn)) {
      PvTZZCoreZ.swapOut()
      PvTZealotExpand.swapIn()
    }
  }

  def barracksCheese: Boolean = enemyStrategy(With.fingerprints.fiveRax, With.fingerprints.proxyRax, With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad) && ! enemyHasShown(Terran.Vulture, Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)

  def doOpeningReactions(): Unit = {
    // Vs. BBS: Zealots into 3-Gate Goon into Reaver (And expand after Reaver if they do)
    // Vs. 11-13: Zealots into 3-Gate Goon into Reaver (And expand after Reaver if they do)
    // Vs. Worker rush: Zealots into 3-Gate Goon
    // Vs. 2-Fac, or possibly any scout-blocking wall-in: Gate-Core-Gate before expand

    if (frame < Minutes(8)() && With.fingerprints.workerRush() && enemies(IsWarrior) == 0) {
      status("ReactToWorkerRush")
      pump(Protoss.Probe, 9)
      if (units(IsWarrior) <= 3) cancel(Protoss.Nexus)
      gasLimitCeiling(50)
            if (units(IsWorker) >= 10)  gasWorkerCeiling(2)
      else  if (units(IsWorker) >= 7)   gasWorkerCeiling(1)
      else                              gasWorkerCeiling(0)
      pump(Protoss.Dragoon, 3)
      pump(Protoss.Zealot, 3)
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(10, Protoss.Probe)
      once(Protoss.Gateway)
      once(12, Protoss.Probe)
      pumpWorkers()
      once(Protoss.Assimilator, Protoss.CyberneticsCore)
      once(2, Protoss.Pylon)
      once(2, Protoss.Gateway)
      once(Protoss.DragoonRange)
    }
    if (frame < Minutes(7)() && With.fingerprints.bunkerRush() && (enemies(IsAll(Terran.Bunker, IsProxied), Terran.Marine) > 0)) {
      status("ReactToBunkerRush")
      pump(Protoss.Dragoon, 3)
      if ( ! haveComplete(Protoss.CyberneticsCore)) {
        pump(Protoss.Zealot, 3)
      }
    }
    if (frame < Minutes(10)() && PvT13Nexus() && With.geography.ourNatural.enemies.exists(e => Terran.Bunker(e) && e.complete)) {
      cancel(Protoss.Nexus)
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      get(3, Protoss.Gateway)
      gasLimitCeiling(250)
      get(Protoss.RoboticsFacility)
      if (Protoss.DragoonRange()) {
        attack()
      }
      return
    }
    if (frame < Minutes(10)() && barracksCheese) {
      status("ReactToRaxCheese")
      gasLimitCeiling(200)
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(10, Protoss.Probe)
      once(Protoss.Gateway)
      once(13, Protoss.Probe)
      once(Protoss.Zealot)


      if (enemyStrategy(With.fingerprints.fiveRax, With.fingerprints.proxyRax, With.fingerprints.bbs) && units(Protoss.Gateway) < 2) {
        gasWorkerCeiling(0)
        cancel(Protoss.Assimilator, Protoss.CyberneticsCore)
      }

      autosupply()
      pumpShuttleAndReavers(2, shuttleFirst = false)
      pump(Protoss.Zealot, 3)
      pumpWorkers()
      if (haveComplete(Protoss.Reaver)) {
        requireMiningBases(2)
      } else {
        cancel(Protoss.Nexus, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.Stargate)
      }
      if (upgradeComplete(Protoss.DragoonRange, 1, Protoss.Dragoon.buildFrames)) {
        pump(Protoss.Dragoon)
      }
      pump(Protoss.Zealot, 7)
      get(2, Protoss.Gateway)
      get(Protoss.Assimilator)
      get(Protoss.CyberneticsCore)
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      get(Protoss.RoboticsFacility)
      get(Protoss.RoboticsSupportBay)
      once(3, Protoss.Gateway)
    }
  }
}
