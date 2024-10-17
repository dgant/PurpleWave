package Planning.Plans.Gameplans.Protoss.FPM

import Debugging.SimpleString
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlacementQuery
import Planning.Plans.Gameplans.All.GameplanImperative
import Planning.Plans.Gameplans.Protoss.PvP.PvPIdeas
import Planning.Plans.Macro.Automatic.Friendly
import ProxyBwapi.Races.Protoss
import Utilities.SwapIf
import Utilities.UnitFilters.{IsTownHall, IsWarrior}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class PvPFPM extends GameplanImperative {

  def enemyHalls: Int = Math.max(1, enemies(Protoss.Nexus))

  def getTownHallsFPM(quantity: Int): Unit = {
    get(quantity, Protoss.Nexus, new PlacementQuery(Protoss.Gateway))
  }

  trait TechStep extends SimpleString {
    protected val baseValue: Double = Math.random()
    final def score: Double = 1000 * Maff.fromBoolean(followThrough) + scoreInner
    def followThrough: Boolean
    def paidOff: Boolean
    def scoreInner: Double
    def executeBuild(): Unit = {
      scoutOn(Protoss.Pylon)
      // This surely is slower than the best build on Fastest Possible Map but we'll let MacroSim sort it out.
      get(8, Protoss.Probe)
      get(Protoss.Pylon)
      get(10, Protoss.Probe)
      get(Protoss.Gateway)
      get(12, Protoss.Probe)
      get(Protoss.Assimilator)
      get(13, Protoss.Probe)
      get(Protoss.CyberneticsCore)
      get(14, Protoss.Probe)
      get(Protoss.Zealot)
      once(2, Protoss.Pylon)
      once(16, Protoss.Probe)
      once(Protoss.Dragoon)
    }
    def executeMain(): Unit
  }
  class TechMacroNexus extends TechStep {
    def followThrough: Boolean = false
    def scoreInner: Double = baseValue * 0.6 + 1.5 * (enemies(IsTownHall) - units(IsTownHall)) * Maff.fromBoolean(safeDefending)
    def paidOff: Boolean = unitsComplete(Protoss.Nexus) > 1 && With.units.ours.filter(Protoss.Nexus).forall(_.complete)
    def executeMain(): Unit = {
      // Say we want a margin of ~20 workers to bother, but also need ~20 on gas
      val workerTarget = With.geography.ourBases.map(b => b.minerals.length + 3 * b.gas.length).sum
      if (units(Protoss.Probe) < workerTarget - 20) {
        getTownHallsFPM(enemyHalls + 1)
      }
    }
    override def executeBuild(): Unit = {
      scoutOn(Protoss.Pylon)
      get(8, Protoss.Probe)
      get(Protoss.Pylon)
      get(13, Protoss.Probe)
      getTownHallsFPM(2)
    }
  }
  class TechTemplar extends TechStep {
    def followThrough: Boolean = units(Protoss.TemplarArchives) > 0
    def paidOff: Boolean = upgradeComplete(Protoss.HighTemplarEnergy) && techComplete(Protoss.PsionicStorm)
    def scoreInner: Double = (
      baseValue
        + 1 * Maff.fromBoolean(enemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))
        - 2 * Maff.fromBoolean(enemyHasShown(Protoss.RoboticsFacility, Protoss.RoboticsSupportBay, Protoss.Observer, Protoss.Observatory, Protoss.Shuttle, Protoss.Reaver)))
    def executeMain(): Unit = {
      get(Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.Forge)
      get(2, Protoss.DarkTemplar)
      buildCannonsAtOpenings(2)
      buildCannonsAtMain(1)
      if (units(Protoss.Gateway) >= 5) {
        upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
        get(Protoss.ZealotSpeed)
      }
      if (units(Protoss.Gateway) >= 7) {
        upgradeContinuously(Protoss.HighTemplarEnergy)
        get(Protoss.PsionicStorm)
      }
    }
  }
  class TechRobo extends TechStep {
    def followThrough: Boolean = units(Protoss.RoboticsFacility) > 0
    def paidOff: Boolean = unitsEver(Protoss.Reaver) > 2
    def scoreInner: Double = (
      baseValue
        + 2 * Maff.fromBoolean(enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.dtRush))
        + 2 * Maff.fromBoolean(enemyDarkTemplarLikely)
      )
    def executeMain(): Unit = {
      get(Protoss.RoboticsFacility, Protoss.Shuttle, Protoss.Observatory, Protoss.Observer, Protoss.RoboticsSupportBay)
      if (enemyDarkTemplarLikely) {
        get(2, Protoss.Observer)
        get(Protoss.ObserverSpeed)
      }
      get(2, Protoss.Reaver)
      get(Protoss.ShuttleSpeed)
    }
  }
  class TechGateways extends TechStep {
    def followThrough: Boolean = false
    def paidOff: Boolean = unitsComplete(Protoss.Gateway) >= 6 && upgradeComplete(Protoss.ZealotSpeed)
    def scoreInner: Double = baseValue - 0.25 + Maff.fromBoolean( ! safeDefending)
    def executeMain(): Unit = {
      get(6, Protoss.Gateway)
      get(Protoss.Forge, Protoss.CitadelOfAdun)
      upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
      get(Protoss.ZealotSpeed)
      get(Protoss.TemplarArchives)
    }
  }
  class TechCarriers extends TechStep {
    def followThrough: Boolean = units(Protoss.Stargate, Protoss.FleetBeacon) >= 2
    def paidOff: Boolean = unitsEver(Protoss.Carrier) >= 7 && units(Protoss.Stargate) >= 4
    def scoreInner: Double = -2
    def executeMain(): Unit = {
      get(3, Protoss.Stargate)
      get(Protoss.FleetBeacon)
      get(3, Protoss.Carrier)
      upgradeContinuously(Protoss.AirDamage) && upgradeContinuously(Protoss.AirArmor)
      get(Protoss.CarrierCapacity)
      get(4, Protoss.Stargate)
    }
  }

  val allTechs: Vector[TechStep] = Vector(
    new TechMacroNexus,
    new TechTemplar,
    new TechRobo,
    new TechGateways,
    new TechCarriers)
  val techQueue = new mutable.Queue[TechStep]
  val techsDone = new ArrayBuffer[TechStep]()

  def doNextTech(): Unit = {
    if (techQueue.nonEmpty) {
      val tech = techQueue.dequeue()
      techsDone += tech
      tech.executeMain()
    }
  }

  def doArmy(): Unit = {
    get(Protoss.DragoonRange)
    if (enemyDarkTemplarLikely)         { pump(Protoss.Observer, 2) }
    if (enemies(Protoss.Observer) == 0) { pump(Protoss.DarkTemplar, 2) }
    if (units(Protoss.Gateway) >= 6)    { pumpRatio(Protoss.HighTemplar, 0, 12, Seq(Friendly(IsWarrior, 0.2))) }
    if (units(Protoss.HighTemplar) > 1) { get(Protoss.PsionicStorm) }
    pumpShuttleAndReavers(6)
    pump(Protoss.Carrier)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
  }

  override def executeBuild(): Unit = {
    techQueue.clear()
    techQueue ++= allTechs.sortBy(-_.score)
    techQueue.head.executeBuild()
  }

  override def executeMain(): Unit = {
    if (safePushing && (foundEnemyBase || unitsComplete(Protoss.DarkTemplar) > 0 || unitsComplete(IsWarrior) >= 12)) {
      attack()
    }
    if (upgradeComplete(Protoss.ShuttleSpeed) || unitsComplete(Protoss.DarkTemplar) > 0) {
      harass()
    }
    status("Tech:" + techQueue.map(_.toString.replace("Tech", "")).mkString("-"))

    get(Protoss.Pylon, Protoss.Gateway, Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Zealot, Protoss.Dragoon)
    PvPIdeas.requireTimelyDetection()

    SwapIf(techQueue.headOption.exists(_.followThrough) || safeDefending, doArmy, doNextTech)

    while (techQueue.nonEmpty && techsDone.lastOption.forall(_.paidOff)) {
      doNextTech()
    }

    get(5, Protoss.Gateway)
    buildGasPumps(Math.floor(0.15 * units(Protoss.Probe) - gas / 200).toInt)
    doNextTech()
    get(8, Protoss.Gateway)
    doNextTech()
    get(12, Protoss.Gateway)
    while (techQueue.nonEmpty) {
      doNextTech()
    }
  }

}
