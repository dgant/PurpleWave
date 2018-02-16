package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers._
import Planning.Plan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class TrainContinuously(
  unitClass                 : UnitClass,
  maximumTotal              : Int = Int.MaxValue,
  maximumConcurrently       : Int = Int.MaxValue,
  maximumConcurrentlyRatio  : Double = 1.0)
    extends Plan {
    
  description.set("Continuously train " + unitClass)
  
  override def onUpdate() {
    if ( ! canBuild) return
    
    val unitsNow                 = currentCount
    val unitsMaximum             = maximumTotal
    val unitsMaximumDesirable    = maxDesirable
    val buildersExisting         = builders
    val buildersOccupied         = buildersExisting.toSeq.map(_.unitClass).distinct.map(With.scheduler.dumbPumps.consumed).sum
    val capacityMaximum          = (buildersExisting.size * maximumConcurrentlyRatio).toInt
    val capacityFinal            = Math.max(0, Vector(maximumConcurrently, capacityMaximum, Math.max(1, capacityMaximum - buildersOccupied)).min)
    val quantityToAdd            = List(unitsMaximum, unitsMaximumDesirable, capacityFinal).min
    val quantityToRequest        = List(unitsMaximum, unitsMaximumDesirable, capacityFinal + unitsNow).min
    
    (unitClass.buildUnitsBorrowed ++ unitClass.buildUnitsSpent).foreach(builderClass =>
      With.scheduler.dumbPumps.consume(builderClass, quantityToAdd))
        
    With.scheduler.request(this, RequestAtLeast(quantityToRequest, unitClass))
  }
  
  protected def canBuild: Boolean = {
    unitClass.buildTechEnabling.forall(With.self.hasTech) &&
    unitClass.buildUnitsEnabling.forall(unitClass => With.units.ours.exists(unit => unit.alive && unit.is(unitClass))) &&
    unitClass.buildUnitsBorrowed.forall(unitClass => With.units.ours.exists(unit => unit.alive && unit.is(unitClass)))
  }
  
  protected def currentCount: Int = {
    // Should this just be unit.alive?
    // Maybe this is compensating for a Scheduler
    With.units.ours.count(unit => unit.aliveAndComplete && matcher.accept(unit))
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
      && builder.framesBeforeBecomingComplete < unitClass.buildFrames
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
