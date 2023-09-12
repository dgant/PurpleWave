package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.?
import Utilities.Time.{GameTime, Minutes}

import scala.collection.mutable

class PvZ1BaseReactive extends PvZ1BaseReactiveUtilities {

  // TODO: No gas during 4-gate reaction
  // TODO: No gas during speedling emergency
  // TODO: Cap gas workers

  protected var economy     : Economy           = Neutral
  protected var opening     : Opening           = _
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
    go (Speedlot,     have(Protoss.CitadelOfAdun))
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

  }
  protected def obsVsLurker(): Unit = {
    if (enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg)) {
      get(Protoss.RoboticsFacility, Protoss.Observatory, Protoss.Observer)
      pump(Protoss.Observer, 2)
    }
  }

  override def executeBuild(): Unit = {
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
    if (opening == Gates910 || opening == Gates910) scoutOn(Protoss.Gateway, 2) else scoutOn(Protoss.Pylon)
  }

  override def executeMain(): Unit = {
    With.blackboard.scoutExpansions.set(false)
    status(economy)
    status(opening)
    status(composition.mkString(""))
    status(anticipateSpeedlings, "Speedlings")
    obsVsLurker()
    requireMiningBases(?(frame > Minutes(10)(), 2, 1))
    pump(Protoss.Dragoon, ?(have(Protoss.Stargate) && enemiesHave(Zerg.Scourge), 2, 1))

    pump(Protoss.Scout)
    pump(Protoss.Zealot)
  }
}
