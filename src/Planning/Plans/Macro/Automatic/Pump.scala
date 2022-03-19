package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Buildables.Get
import Macro.Scheduling.MacroCounter
import Planning.Plan
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Pump(
  unitClass           : UnitClass,
  maximumTotal        : Int = Int.MaxValue,
  maximumConcurrently : Int = Int.MaxValue)
    extends Plan {

  protected def maxDesirable: Int = Int.MaxValue

  val builderClass: UnitClass = unitClass.whatBuilds._1
  
  override def onUpdate() {
    if ( ! canBuild) return
  
    val unitsComplete       = With.units.ours.filter(unitClass).map(MacroCounter.countComplete(_)(unitClass)).sum
    val unitsToAddCeiling   = Math.max(0, Math.min(maximumTotal, maxDesirable) - unitsComplete) // TODO: Clamp Nukes to #Silos
    val larvaSpawning       = if (builderClass == Zerg.Larva) With.units.countOurs(MatchAnd(MatchHatchlike, MatchComplete)) else 0
    val buildersExisting    = getBuildersExisting
    val buildersTotal       = buildersExisting.size + larvaSpawning

    val minerals            = With.self.minerals  // To improve: Measure existing expediture commitments
    val gas                 = With.self.gas       // To improve: Measure existing expediture commitments
    val mineralPrice        = unitClass.mineralPrice
    val gasPrice            = unitClass.gasPrice
    val budgetedByMinerals  = if (mineralPrice  <= 0) 400 else (minerals + With.accounting.ourIncomePerFrameMinerals * unitClass.buildFrames) / mineralPrice
    val budgetedByGas       = if (gasPrice      <= 0) 400 else (gas      + With.accounting.ourIncomePerFrameGas      * unitClass.buildFrames) / gasPrice
    val budgeted            = Math.max(1, Math.ceil(Math.min(budgetedByMinerals, budgetedByGas))) // 3/4/2020 change: To encourage saving budget for eg. Dragoon instead of burning on Zealot, using ceil instead of round

    val buildersToConsume   = Math.max(0, Vector(maximumConcurrently, buildersTotal, budgeted, unitsToAddCeiling / unitClass.copiesProduced).min.toInt)
    val unitsToAdd          = buildersToConsume * unitClass.copiesProduced
    val unitsToRequest      = unitsComplete + unitsToAdd

    // This check is necessitated by our tendency to request Scourge even when unitsToAdd is 0
    if (unitsToAdd == 0) return

    With.scheduler.request(this, Get(unitsToRequest, unitClass))
  }

  final protected def canBuild: Boolean = (
    unitClass.buildTechEnabling.forall(With.self.hasTech)
    && unitClass.buildUnitsEnabling.forall(With.units.existsOurs(_))
    && unitClass.buildUnitsBorrowed.forall(With.units.existsOurs(_))
  )

  final protected def getBuildersExisting: Iterable[FriendlyUnitInfo] = With.units.ours
    .view
    .filter(builder =>
      builder.alive
        && builderClass(builder)
        && MacroCounter.countComplete(builder)(builderClass) > 0
        && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
        && ( ! unitClass.requiresPsi                                      || builder.powered)
        && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
        && ( ! unitClass.isAddon                                          || unitClass.buildUnitsEnabling.forall(t => With.units.ours.exists(u => u.completeOrNearlyComplete && u.is(t)))) // Hack -- don't reserve buildings before we have the tech to build the addon.
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))

  description.set(f"Pump $unitClass")
}
