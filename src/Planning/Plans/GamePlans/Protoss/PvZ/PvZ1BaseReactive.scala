package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Mathematics.Maff
import Planning.Compositor
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.{?, SwapIf}
import Utilities.Time.{GameTime, Minutes}

import scala.collection.mutable

class PvZ1BaseReactive extends PvZ1BaseReactiveUtilities {

  // TODO: No gas during 4-gate reaction
  // TODO: No gas during speedling emergency
  // TODO: Cap gas workers

  protected var economy     : Economy           = Neutral
  protected var opening     : Opening           = Gates1012
  protected var composition : Seq[Composition]  = Seq.empty

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
      case Aggro  =>  Seq(Gates1012,  Gates910,   CoreZ,    ZCoreZ,     ZZCoreZ,    GateNexus,  NexusFirst)
      case Greedy =>  Seq(NexusFirst, GateNexus,  CoreZ,    ZCoreZ,     ZZCoreZ,    Gates1012,  Gates910)
      case _      =>  Seq(CoreZ,      ZCoreZ,     ZZCoreZ,  NexusFirst, GateNexus,  Gates1012,  Gates910) })

    // Khala advises: Vs. 9 Pool, ZZCoreZ; otherwise, ZCoreZ.

    if (haveComplete(Protoss.Gateway))      ban(NexusFirst)
    if (haveEver(Protoss.Zealot))           ban(NexusFirst, CoreZ)
    if (haveComplete(Protoss.Assimilator))  ban(NexusFirst, GateNexus,  Gates1012,  Gates910)
    if (With.fingerprints.twoHatchMain())   ban(NexusFirst, GateNexus)
    if (poolMin         <= timing4p)        ban(NexusFirst, GateNexus,  CoreZ,      ZCoreZ, ZZCoreZ)
    if (poolMin         <= timing9p)        ban(NexusFirst, GateNexus,  CoreZ,      ZCoreZ)
    if (poolMin         <= timingOp)        ban(NexusFirst, CoreZ)
    if (poolMin         <= timing12p)       ban(NexusFirst)
    if (pool.exists(_   >= timing10h))      ban(ZCoreZ,     ZZCoreZ,    Gates1012,  Gates910)

    if (allowed.isEmpty) return
    opening = allowed.head

    consider(GateNexus, poolMax         >= timingOp && economy == Greedy)
    consider(Gates1012, poolMinRecently <= timing4p)
    consider(Gates910,  poolMaxRecently <= timing4p)
  }

  protected def chooseComposition(): Unit = {
    val static = enemies(Zerg.CreepColony, Zerg.SunkenColony)

    composition = Seq.empty

    def on(c: Composition*): Boolean = c.exists(composition.contains)
    def go(c: Composition, p: Boolean): Unit = if(p && ! composition.contains(c)) composition :+= c
    def goo(c: Composition, max: Int, p: Boolean = true): Unit = go(c, composition.length <= max && p)

    go (Goon,         upgradeStarted(Protoss.DragoonRange))
    go (Speedlot,     have(Protoss.Forge))
    go (Stargate,     have(Protoss.Stargate))
    go (Reaver,       have(Protoss.Shuttle, Protoss.RoboticsSupportBay) || (have(Protoss.RoboticsFacility) && ! on(Goon, Speedlot, Stargate)))
    goo(Reaver,   0,  enemyHydralisksLikely)
    goo(Reaver,   0,  enemyLurkersLikely)
    goo(Speedlot, 0,  anticipateSpeedlings)
    goo(Stargate, 0,  enemyMutalisksLikely)
    goo(Stargate, 0,  With.fingerprints.twoHatchGas())
    goo(Speedlot, 0,  With.fingerprints.threeHatchGas())
    goo(Reaver,   0,  static > 1)
    goo(Goon,     0)
    goo(Stargate, 1,  enemyMutalisksLikely && ! on(Goon))
    goo(Reaver,   1,  static > 2)
    goo(Reaver,   1,  on(Stargate) && enemyHydralisksLikely)
    goo(Reaver,   2,  on(Stargate) && enemyHydralisksLikely)
  }

  protected def capGas(): Unit = {
    if (have(Protoss.TemplarArchives)) return
  }
  protected def obsVsLurker(): Unit = {
    if (enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg)) {
      get(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      pump(Protoss.Observer, 2)
    }
  }

  override def executeBuild(): Unit = {
    new MeldArchons()()
    chooseOpening()
    chooseComposition()
    capGas()
    opening match {
      case Gates910   => open910()
      case Gates1012  => open1012()
      case CoreZ      => openCoreZ()
      case ZCoreZ     => openZCoreZ()
      case ZZCoreZ    => openZZCoreZ()
      case GateNexus  => openGateNexus()
      case NexusFirst => openNexusFirst()
      case _          => open1012()
    }
    if (bases <= 1 && anticipateSpeedlings) {
      get(3, Protoss.Zealot)
      get(2, Protoss.Gateway)
      get(7, Protoss.Zealot)
      get(3, Protoss.Gateway)
    }
    if      (enemyRecentStrategy(With.fingerprints.twelveHatch))  scoutOn(Protoss.Pylon)
    else if (opening == Gates910 || opening == Gates1012)         scoutOn(Protoss.Gateway, 2)
    else                                                          scoutOn(Protoss.Pylon)
  }

  private val compositor = new Compositor
  override def executeMain(): Unit = {
    With.blackboard.scoutExpansions.set(false)
    status(economy)
    status(opening)
    status(composition.mkString(""))
    status(anticipateSpeedlings, "Speedlings")

    obsVsLurker()
    requireMiningBases(?(frame > Minutes(10)(), 2, 1))

    SwapIf(
      safeDefending || supplyUsed200 > 30,
      buildArmy(),
      buildTech())
  }

  def buildTech(): Unit = {
    get(Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore)
    composition.foreach {
      case Goon =>
        get(Protoss.DragoonRange)
        if (composition.length == 1) {
          get(4, Protoss.Gateway)
        }
      case Speedlot =>
        get(Protoss.Forge)
        get(Protoss.CitadelOfAdun)
        get(Protoss.GroundDamage)
        get(Protoss.ZealotSpeed)
        get(Protoss.TemplarArchives)
        get(Protoss.DarkTemplar)
        get(2, Protoss.HighTemplar)
        if (composition.length == 1) {
          get(5, Protoss.Gateway)
        }
      case Reaver =>
        get(Protoss.RoboticsFacility)
        SwapIf(
          safeDefending,
          {
            get(Protoss.RoboticsSupportBay)
            get(2, Protoss.Reaver)
          },
          get(Protoss.Shuttle))
        if (composition.length == 1) {
          get(2, Protoss.Gateway)
        }
      case Stargate =>
        get(Protoss.Stargate)
        get(2, Protoss.Gateway)
        if (composition.length == 1) {
          get(2, Protoss.Stargate)
        }
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
    val denominator     =   weighZerglings + weighHydralisks + weighMutalisks + weighSunkens
    weighZerglings      /=  Maff.nanToOne(denominator)
    weighHydralisks     /=  Maff.nanToOne(denominator)
    weighMutalisks      /=  Maff.nanToOne(denominator)

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
      val goonsVsScourge = ?(composition.contains(Stargate) && enemyHasShown(Zerg.Scourge), 2, 0)
      compositor.setNeed(Protoss.Dragoon, 5.0)
      compositor.capGoal(Protoss.Dragoon, goonsVsScourge)
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

    pump(Protoss.Scout)
    pump(Protoss.HighTemplar)
    pump(Protoss.Zealot)
    pump(Protoss.Gateway, 5 * miningBases - units(Protoss.Stargate) - units(Protoss.RoboticsSupportBay))
    requireMiningBases(2) // Unlikely to happen but maybe useful if we run out of building room at home
  }
}
