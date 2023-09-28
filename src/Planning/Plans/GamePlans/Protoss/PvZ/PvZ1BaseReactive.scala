package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Placement.Access.PlaceLabels
import Planning.Compositor
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Buildable
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZExpand, PvZGoon, PvZMuscle, PvZReaver, PvZSpeedlot, PvZTech}
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.{IsHatchlike, IsWarrior, IsWorker}
import Utilities.{?, SwapIf}

import scala.collection.mutable

class PvZ1BaseReactive extends PvZ1BaseReactiveUtilities {

  protected def economy       : Economy           = ?(PvZTech(committedTech), Tech, ?(PvZExpand(committedExpand), Expand, Muscle))
  protected var timingAttack  : Boolean           = false
  protected var opening       : Opening           = Gates1012
  protected var composition   : Seq[Composition]  = Seq.empty

  protected def chooseOpening(): Unit = {
    if (frame > GameTime(3, 30)())      return
    if (units(Protoss.Gateway)    > 1)  return
    if (units(Protoss.Nexus)      > 1)  return
    if (unitsEver(Protoss.Zealot) > 1)  return
    if (have(Protoss.CyberneticsCore))  return

    val allowed = new mutable.ArrayBuffer[Opening]()

    def ban(openings: Opening*): Unit = allowed --= openings
    def consider(candidate: Opening, predicate: => Boolean): Unit = if (allowed.contains(candidate) && predicate) opening = candidate

    allowed ++= (economy match {
      case Muscle =>  Seq(Gates1012,  Gates910,   CoreZ,    ZCoreZ,     ZZCoreZ,    GateNexus,  NexusFirst)
      case Expand =>  Seq(NexusFirst, GateNexus,  CoreZ,    ZCoreZ,     ZZCoreZ,    Gates1012,  Gates910)
      case Tech   =>  Seq(CoreZ,      ZCoreZ,     ZZCoreZ,  NexusFirst, GateNexus,  Gates1012,  Gates910) })

    // Per Khala: Vs. 9 Pool, ZZCoreZ; Vs. Hatch-first, CoreZ; otherwise, ZCoreZ.

    if (haveComplete(Protoss.Gateway))      ban(NexusFirst)
    if (haveEver(Protoss.Zealot))           ban(NexusFirst, CoreZ)
    if (haveComplete(Protoss.Assimilator))  ban(NexusFirst, GateNexus,  Gates1012,  Gates910)
    if (With.fingerprints.twoHatchMain())   ban(NexusFirst, GateNexus)
    if (anticipateSpeedlings)               ban(NexusFirst, GateNexus,  CoreZ,      ZCoreZ, ZZCoreZ)
    if (poolMin         <= timing4p)        ban(NexusFirst, GateNexus,  CoreZ,      ZCoreZ, ZZCoreZ)
    if (poolMin         <= timing9p)        ban(NexusFirst, GateNexus,  CoreZ,      ZCoreZ)
    if (poolMin         <= timingOp)        ban(NexusFirst,             CoreZ)
    if (poolMin         <= timing12p)       ban(NexusFirst,             CoreZ)
    if (pool.exists(_   >= timing10h))      ban(ZCoreZ,     ZZCoreZ,    Gates1012,  Gates910)
    if (With.fingerprints.gasSteal())       ban(ZZCoreZ,    ZCoreZ,     CoreZ)

    if (allowed.isEmpty) return
    opening = allowed.head

    consider(GateNexus, poolMax         >= timingOp && economy == Expand)
    consider(Gates1012, poolMinRecently <= timing4p)
    consider(Gates910,  poolMaxRecently <= timing4p)
  }

  def opened(o: Opening*): Boolean = o.contains(opening)
  def on  (c: Composition*)                               : Boolean = c.exists(composition.contains)
  def go  (c: Composition,            p: Boolean = true)  : Unit    = if (p && ! composition.contains(c)) composition :+= c
  def goo (c: Composition, max: Int,  p: Boolean = true)  : Unit    = go(c, composition.length <= max && p)

  protected def chooseComposition(): Unit = {
    val static = enemies(Zerg.CreepColony, Zerg.SunkenColony)

    composition = Seq.empty

    go (Stargate,     have(Protoss.Stargate))
    go (Reaver,       have(Protoss.RoboticsFacility, Protoss.Shuttle, Protoss.RoboticsSupportBay, Protoss.Reaver))
    go (Speedlot,     have(Protoss.CitadelOfAdun) || (have(Protoss.Forge) && ! enemyLurkersLikely))
    go (Goon,         upgradeComplete(Protoss.DragoonRange) || units(Protoss.Dragoon) > 3)
    goo(Reaver,   0,  have(Protoss.RoboticsFacility) || enemyHydralisksLikely || enemyLurkersLikely)
    goo(Speedlot, 0,  anticipateSpeedlings)
    goo(Goon,     0,  enemyMutalisksLikely && units(Protoss.Gateway) >= 3)
    goo(Stargate, 0,  enemyMutalisksLikely)
    goo(Stargate, 0,  With.fingerprints.twoHatchGas()   && enemyHasShown(Zerg.Lair))
    goo(Speedlot, 0,  With.fingerprints.threeHatchGas() && enemies(IsHatchlike) > 2)
    goo(Reaver,   0,  static > 1)
    goo(Goon,     0,  PvZGoon     (committedGoon))
    goo(Reaver,   0,  PvZReaver   (committedReaver))
    goo(Speedlot, 0,  PvZSpeedlot (committedSpeedlot))
    goo(Stargate, 1,  enemyMutalisksLikely              && ! on(Goon))
    goo(Goon,     1,  enemyMutalisksLikely              && on(Stargate))
    goo(Reaver,   1,  enemyHydralisksLikely             && on(Stargate))
    goo(Speedlot, 1,  With.fingerprints.threeHatchGas() && on(Reaver))
    goo(Speedlot, 1,  miningBases > 1)
    goo(Reaver,   2,  static > 2)
    goo(Goon,     2,  enemyMutalisksLikely)
    goo(Reaver,   2,  units(Protoss.Zealot, Protoss.Dragoon) > 24)
    goo(Reaver,   3,  have(Protoss.RoboticsFacility))
  }

  protected def capGas(): Unit = {
    if (have(Protoss.TemplarArchives)) return
    if (anticipateSpeedlings && units(IsWarrior) < 9 && units(Protoss.Gateway) < 3) {
      gasWorkerCeiling(0)
    } else if (composition.length == 1 && ! on(Speedlot)) {
      gasLimitCeiling(300)
    }
  }
  protected def detectionVsLurker(): Unit = {
    if (enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg)) {
      status("Lurkers")
      get(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      if (enemies(Zerg.Lurker) > 0 && ! have(Protoss.Observer)) {
        status("LurkerDanger")
        buildCannonsAtOpenings(1)
      }
      pump(Protoss.Observer, 2)
    }
  }

  private def committedSpeedlot = haveEver(Protoss.Forge)
  private def committedReaver   = haveEver(Protoss.RoboticsFacility)
  private def committedGoon     = upgradeStarted(Protoss.DragoonRange)
  private def committedTech     = haveEver(Protoss.Assimilator)
  private def committedExpand   = With.geography.maxMiningBasesOurs >= 2

  override def executeBuild(): Unit = {
    new MeldArchons()()
    chooseOpening()
    chooseComposition()
    capGas()
    if (anticipateSpeedlings) {
      openVsSpeedlings()
    } else {
      opening match {
        case Gates910   => open910()
        case Gates1012  => ?(economy == Expand, open1012Expand(), open1012Gas())
        case CoreZ      => openCoreZ()
        case ZCoreZ     => openZCoreZ()
        case ZZCoreZ    => openZZCoreZ()
        case GateNexus  => openGateNexus()
        case NexusFirst => openNexusFirst()
        case _          => open1012Gas()
      }
    }
    scoutOn(Protoss.Pylon)

    if ( ! committedSpeedlot) PvZSpeedlot .deactivate()
    if ( ! committedReaver)   PvZReaver   .deactivate()
    if ( ! committedGoon)     PvZGoon     .deactivate()
    if ( ! committedExpand)   PvZExpand   .deactivate()
    if ( ! committedTech)     PvZTech     .deactivate()
  }

  private val compositor = new Compositor
  override def executeMain(): Unit = {
    With.blackboard.scoutExpansions.set(false)

    var canAttackEarly    = opened(Gates910, Gates1012)
    canAttackEarly      ||= opened(ZZCoreZ, ZCoreZ)  && enemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
    canAttackEarly      ||= haveComplete(Protoss.Scout,  Protoss.DarkTemplar)
    canAttackEarly      &&= unitsComplete(IsWarrior) >= 3 || unitsEver(IsWarrior) >= 5 || With.fingerprints.twelveHatch()
    canAttackEarly      &&= confidenceAttacking11 > 0.2 + 0.1 * enemiesComplete(Zerg.SunkenColony)
    canAttackEarly      &&= ! With.fingerprints.twoHatchMain()

    val needToPushLurkers = enemyLurkersLikely && ! haveComplete(Protoss.PhotonCannon, Protoss.Observer)

    val allIn = enemiesHave(Zerg.Mutalisk) && ! haveComplete(Protoss.Corsair) && unitsComplete(Protoss.Dragoon) < 6

    timingAttack ||= unitsComplete(IsWarrior) >= 24 // Safety valve in case build gets disrupted somehow
    timingAttack ||= unitsComplete(Protoss.Reaver) >= 2     && haveComplete(Protoss.Shuttle)
    timingAttack ||= upgradeComplete(Protoss.ZealotSpeed)   && upgradeComplete(Protoss.GroundDamage) && haveComplete(Protoss.Archon)
    timingAttack ||= upgradeComplete(Protoss.DragoonRange)  && unitsComplete(Protoss.Dragoon) >= 10
    timingAttack ||= haveComplete(Protoss.Scout)
    timingAttack ||= enemyMutalisksLikely

    aggression(?(allIn || timingAttack, 1.5, 0.75))
    attack(allIn || canAttackEarly || timingAttack || needToPushLurkers)
    status(allIn || canAttackEarly && ! timingAttack, "EarlyAttack")
    status(allIn, "AllIn")
    status(timingAttack, "TimingAttack")
    status(economy)
    status(opening)
    status(composition.mkString(""))
    status(anticipateSpeedlings, "Speedlings")
    With.blackboard.scoutExpansions.set(enemyBases == 0 || supplyUsed200 >= 100)

    detectionVsLurker()

    requireMiningBases(?(frame > Minutes(10)(), 2, 1))
    if (timingAttack && With.scouting.enemyProximity < 0.75 && (PvZExpand(committedExpand) || PvZMuscle())) {
      requireMiningBases(2)
    }

    SwapIf(
      supplyUsed200 >= 60  - 30 * confidenceDefending01 - 4 * units(Protoss.Gateway),
      buildArmy(),
      buildTech())

    if (PvZExpand(committedExpand)) {
      get(Math.max(2, 3 - units(Protoss.RoboticsFacility, Protoss.Stargate)), Protoss.Gateway)
      requireMiningBases(2)
    }
    get(5 * miningBases - units(Protoss.RoboticsFacility, Protoss.Stargate), Protoss.Gateway)
    requireMiningBases(2) // Unlikely to happen but maybe useful if we run out of building room at home
  }

  def buildTech(): Unit = {
    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    if (units(Protoss.Gateway) >= 5 || units(IsWorker) > 35) {
      buildGasPumps()
    }

    composition.foreach {
      case Goon =>
        cancelRoboUnlessLurker()
        cancelAlternative(Protoss.CitadelOfAdun, Protoss.Forge)
        get(Protoss.DragoonRange)
      case Speedlot =>
        cancelRoboUnlessLurker()
        cancelRangeUnlessGoons()
        val makeCorsairs = ! anticipateSpeedlings && ! enemyHydralisksLikely
        if (makeCorsairs) {
          get(Protoss.Stargate)
          get(Protoss.Corsair)
        } else {
          cancelAlternative(Protoss.Stargate)
        }
        get(Protoss.Forge)
        get(Protoss.GroundDamage)
        if (makeCorsairs) {
          get(2, Protoss.Corsair)
        }
        get(Protoss.CitadelOfAdun)
        get(Protoss.ZealotSpeed)
        if (enemyMutalisksLikely) {
          buildCannonsAtMain(2, PlaceLabels.DefendAir)
        }
        get(Protoss.TemplarArchives)
        if (have(Protoss.Corsair) || (anticipateSpeedlings && ! safeDefending)) {
          get(Protoss.DarkTemplar)
        }
        get(2, Protoss.HighTemplar)
      case Reaver =>
        cancelAlternative(Protoss.Stargate, Protoss.CitadelOfAdun, Protoss.Forge)
        cancelRangeUnlessGoons()
        get(Protoss.RoboticsFacility)
        SwapIf(
          safeDefending,
          {
            get(Protoss.RoboticsSupportBay)
            get(2, Protoss.Reaver)
          },
          {
            get(Protoss.CitadelOfAdun)
            get(Protoss.Shuttle)
            get(Protoss.ZealotSpeed)
          })
      case Stargate =>
        cancelRoboUnlessLurker()
        cancelRangeUnlessGoons()
        cancelAlternative(Protoss.Forge)
        get(Protoss.Stargate)
        if (enemyMutalisksLikely || ! enemyHydralisksLikely) {
          get(Protoss.Corsair)
        }
        if (enemyMutalisksLikely) {
          get(Protoss.DragoonRange)
        } else {
          get(Protoss.CitadelOfAdun)
          get(Protoss.ZealotSpeed)
        }
    }
  }

  private def cancelAlternative(buildables: Buildable*): Unit = {
    if (composition.length <= 1) {
      cancel(buildables: _*)
    }
  }

  private def cancelRoboUnlessLurker(): Unit = {
    if (composition.length <= 1 && ! enemyLurkersLikely) {
      cancel(Protoss.RoboticsFacility)
    }
  }

  private def cancelRangeUnlessGoons(): Unit = {
    if (composition.length <= 1 && units(Protoss.Dragoon) < 3) {
      cancel(Protoss.DragoonRange)
    }
  }

  def buildArmy(): Unit = {
    val countZerglings  =   Math.max(enemies(Zerg.Zergling),  ?(enemyHydralisksLikely, 0, Math.max(8, 4 + enemies(Zerg.Zergling))))
    val countHydralisks =   Math.max(enemies(Zerg.Hydralisk), ?(enemyHydralisksLikely, 4, 0))
    val countMutalisks  =   Math.max(enemies(Zerg.Mutalisk),  ?(enemyMutalisksLikely && ! enemyHydralisksLikely,  8, 0))
    val countSunkens    =   enemies(Zerg.SunkenColony) * 2 * Math.max(0, confidenceDefending01 - 0.5)
    var weighZerglings  =   0.25  * countZerglings
    var weighHydralisks =   1.0   * countHydralisks
    var weighMutalisks  =   2.0   * countMutalisks
    var weighSunkens    =   4.0   * countSunkens
    val denominator     =   Math.max(0.01, weighZerglings + weighHydralisks + weighMutalisks + weighSunkens)
    weighZerglings      /=  denominator
    weighHydralisks     /=  denominator
    weighMutalisks      /=  denominator
    weighSunkens        /=  denominator

    val weighZealots  = weighZerglings  + weighSunkens
    val weighDragoons = weighHydralisks + weighMutalisks
    val weighArchons  = weighZerglings  + weighMutalisks  + Math.max(0, units(Protoss.HighTemplar) % 2 + (gas - 100) / 150)
    val weighReavers  = weighHydralisks + weighSunkens
    val weighCorsairs = weighMutalisks
    val weighScouts   = weighZerglings  + weighSunkens

    compositor.reset()
    compositor.setNeed(Protoss.Zealot, weighZealots)
    compositor.setGoal(Protoss.Zealot, 0.35 * countZerglings)
    if (composition.contains(Goon)) {
      compositor.setNeed(Protoss.Dragoon, weighDragoons)
      compositor.setGoal(Protoss.Dragoon, 6 + enemies(Zerg.Mutalisk) + enemies(Zerg.Hydralisk))
    } else {
      compositor.setNeed(Protoss.Dragoon, 5.0)
      compositor.capGoal(Protoss.Dragoon, ?(composition.contains(Stargate) && enemyHasShown(Zerg.Scourge), 2, 1))
    }

    if (composition.contains(Reaver)) {
      compositor.setNeed(Protoss.Reaver,  weighReavers)
      compositor.setNeed(Protoss.Shuttle, weighReavers * 4)
      compositor.setGoal(Protoss.Reaver,  6)
      compositor.capGoal(Protoss.Shuttle,  units(Protoss.Reaver) / 2)
    }

    if (have(Protoss.TemplarArchives)) {
      compositor.setNeed(Protoss.HighTemplar, weighArchons)
      compositor.setGoal(Protoss.HighTemplar, 4)
    }

    if (have(Protoss.Stargate)) {
      compositor.setNeed(Protoss.Corsair, weighCorsairs)
      compositor.capGoal(Protoss.Corsair, Math.max(countMutalisks, ?(enemyHydralisksLikely, 1, 3)))
    }

    compositor.produceAndPump()

    pump(Protoss.HighTemplar)
    if ( ! enemyHydralisksLikely) {
      pump(Protoss.Scout)
    }
    pump(Protoss.Zealot)
  }
}
