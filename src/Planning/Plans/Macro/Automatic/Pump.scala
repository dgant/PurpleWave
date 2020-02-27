package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class Pump(
  unitClass                 : UnitClass,
  maximumTotal              : Int = Int.MaxValue,
  maximumConcurrently       : Int = Int.MaxValue,
  maximumConcurrentlyRatio  : Double = 1.0)
    extends Plan {
    
  description.set("Continuously train " + unitClass)
  
  override def onUpdate() {
    if ( ! canBuild) return
  
    val doubleEggMultiplier       = if (unitClass.isTwoUnitsInOneEgg) 2 else 1
    val unitsNow                  = PumpCount.currentCount(unitClass)
    val unitsToAddCeiling         = Math.max(0, Math.min(maximumTotal, maxDesirable) - unitsNow) // TODO: Clamp Nukes to #Silos
    val buildersSpawning          = if (unitClass.whatBuilds._1 == Zerg.Larva) With.units.countOurs(UnitMatchAnd(UnitMatchHatchery, UnitMatchComplete)) else 0
    val buildersExisting          = builders.toVector
    val buildersReserved          = buildersExisting.view.map(_.unitClass).distinct.map(With.scheduler.macroPumps.pumpsConsumed).sum - doubleEggMultiplier * With.scheduler.macroPumps.buildeesPumped(unitClass)
    val buildersReadiness         = getBuilderReadiness(buildersExisting)
    val buildersTotal             = buildersExisting.size + buildersSpawning
    val buildersAllocatable       = Math.max(0, Math.min(buildersTotal * maximumConcurrentlyRatio, buildersTotal - buildersReserved))
    val builderOutputCap          = Math.round(buildersReadiness * buildersAllocatable)
    
    val minerals                  = With.self.minerals  // To improve: Measure existing expediture commitments
    val gas                       = With.self.gas       // To improve: Measure existing expediture commitments
    val mineralPrice              = unitClass.mineralPrice
    val gasPrice                  = unitClass.gasPrice
    val budgetedByMinerals        = if (mineralPrice  <= 0) 400 else (minerals + (1.0 - buildersReadiness) * With.economy.ourIncomePerFrameMinerals * unitClass.buildFrames) / mineralPrice
    val budgetedByGas             = if (gasPrice      <= 0) 400 else (gas      + (1.0 - buildersReadiness) * With.economy.ourIncomePerFrameGas      * unitClass.buildFrames) / gasPrice
    val budgeted                  = Math.max(1, Math.round(Math.min(budgetedByMinerals, budgetedByGas)))
    
    val buildersToConsume         = Math.max(0, Vector(maximumConcurrently, builderOutputCap, budgeted, unitsToAddCeiling / doubleEggMultiplier).min.toInt)
    val unitsToAdd                = buildersToConsume * doubleEggMultiplier
    val unitsToRequest            = unitsNow + unitsToAdd
  
    // This check is necessitated by our tendency to request Scourge even when unitsToAdd is 0
    if (unitsToAdd == 0) return

    With.scheduler.request(this, Get(unitsToRequest, unitClass))
  }
  
  private def getBuilderReadiness(builders: Iterable[FriendlyUnitInfo]): Double = {
    val output = ByOption
      .mean(builders.map(b => 1.0 - Math.min(1.0, Math.max(b.remainingTrainFrames, b.remainingCompletionFrames).toDouble / unitClass.buildFrames)))
      .getOrElse(0.5)
    
    output
  }
  
  protected def canBuild: Boolean = (
    unitClass.buildTechEnabling.forall(With.self.hasTech)
    && unitClass.buildUnitsEnabling.forall(With.units.existsOurs(_))
    && unitClass.buildUnitsBorrowed.forall(With.units.existsOurs(_))
  )
  
  protected def builders: Iterable[FriendlyUnitInfo] = With.units.ours
    .view
    .filter(builder =>
      builder.alive
        && builder.is(unitClass.whatBuilds._1)
        && builder.remainingCompletionFrames < unitClass.buildFrames
        && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
        && ( ! unitClass.requiresPsi                                      || builder.powered)
        && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
        && ( ! unitClass.isAddon                                          || unitClass.buildUnitsEnabling.forall(t => With.units.ours.exists(u => u.completeOrNearlyComplete && u.is(t)))) // Hack -- don't reserve buildings before we have the tech to build the addon.
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))
  
  protected def buildCapacity: Int = {
    Vector(
      builders.size * (if (unitClass.isTwoUnitsInOneEgg) 2 else 1),
      if (unitClass.isAddon) builders.count(_.addon.isEmpty) else Int.MaxValue,
      if (unitClass.supplyRequired == 0) 400 else (400 - With.self.supplyUsed) / unitClass.supplyRequired
    ).min
  }
  
  protected def maxDesirable: Int = Int.MaxValue
}
