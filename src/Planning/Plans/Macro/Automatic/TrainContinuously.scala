package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers._
import Planning.Plan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class TrainContinuously(
  unitClass                 : UnitClass,
  maximumTotal              : Int = Int.MaxValue,
  maximumConcurrently       : Int = Int.MaxValue,
  maximumConcurrentlyRatio  : Double = 1.0)
    extends Plan {
    
  description.set("Continuously train " + unitClass)
  
  override def onUpdate() {
    if ( ! canBuild) return
  
    val doubleEggMultiplier       = if (unitClass.isTwoUnitsInOneEgg) 2 else 1
    val unitsNow                  = currentCount
    val unitsToAddCeiling         = Math.max(0, Math.min(maximumTotal, maxDesirable) - unitsNow)
    val buildersSpawning          = if (unitClass.whatBuilds._1 == Zerg.Larva) With.units.countOurs(UnitMatchAnd(UnitMatchHatchery, UnitMatchComplete)) else 0
    val buildersExisting          = builders.toVector
    val buildersReserved          = buildersExisting.map(_.unitClass).distinct.map(With.scheduler.macroPumps.consumed).sum
    val buildersReadiness         = getBuilderReadiness(buildersExisting)
    val buildersTotal             = buildersExisting.size + buildersSpawning
    val buildersAllocatable       = Math.max(0, Math.min(buildersTotal * maximumConcurrentlyRatio, buildersTotal - buildersReserved))
    val builderOutputCap          = Math.max(Math.round(buildersReadiness * buildersAllocatable), if (buildersExisting.nonEmpty) 1 else 0)
    
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
    
    (unitClass.buildUnitsBorrowed ++ unitClass.buildUnitsSpent).foreach(builderClass =>
      With.scheduler.macroPumps.consume(builderClass, buildersToConsume))
    With.scheduler.request(this, Get(unitsToRequest, unitClass))
  }
  
  private def getBuilderReadiness(builders: Vector[FriendlyUnitInfo]): Double = {
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
  
  protected def currentCount: Int = {
    // Should this just be unit.alive?
    // Maybe this is compensating for a Scheduler
    With.units.ours
      .toVector
      .map(unit =>
        if (unit.alive && matcher.accept(unit)) {
          1
        }
        else if (unit.is(Zerg.Egg) && unit.buildType == unitClass) {
          if (unitClass.isTwoUnitsInOneEgg) 2 else 1
        }
        else {
          0
        })
      .sum
  }
  
  protected val matcher =
    UnitMatchOr(
      new UnitMatcher {
        override def accept(unit: UnitInfo): Boolean = unit.is(Zerg.Egg) && unit.friendly.exists(_.buildType == unitClass)
      },
      if (unitClass == Terran.SiegeTankSieged || unitClass == Terran.SiegeTankUnsieged) {
        UnitMatchSiegeTank
      }
      else if (unitClass == Zerg.Hatchery) {
        UnitMatchHatchery
      }
      else if (unitClass == Zerg.Lair) {
        UnitMatchLair
      }
      else if (unitClass == Zerg.Spire) {
        UnitMatchSpire
      }
      else unitClass)
  
  protected def builders: Set[FriendlyUnitInfo] = With.units.ours.filter(builder =>
    builder.alive
      && builder.is(unitClass.whatBuilds._1)
      && builder.remainingCompletionFrames < unitClass.buildFrames
      && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
      && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
      && ( ! unitClass.isAddon                                          || unitClass.buildUnitsEnabling.forall(t => With.units.ours.exists(u => u.complete && u.is(t)))) // Hack -- don't reserve buildings before we have the tech to build the addon.
      && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
      && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))
  
  protected def buildCapacity: Int = {
    Vector(
      builders.size * (if (unitClass.isTwoUnitsInOneEgg) 2 else 1),
      if (unitClass.isAddon) builders.count(_.addon.isEmpty) else Int.MaxValue,
      if (unitClass.supplyRequired == 0) 400 else (400 - With.self.supplyUsed) / unitClass.supplyRequired
    ).min
  }
  
  protected def maxDesirable: Int = {
    Int.MaxValue
  }
}
