package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers._
import Planning.Plan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class TrainContinuously(
  unitClass           : UnitClass,
  maximum             : Int = Int.MaxValue,
  maximumConcurrently : Int = Int.MaxValue)
    extends Plan {
    
  description.set("Continuously train " + unitClass)
  
  override def onUpdate() {
    if ( ! canBuild) return
    
    val unitsMaximum          = maximum
    val unitsMaximumDesirable = maxDesirable
    val buildersAvailable     = builders
    val buildersOccupied      = builders.toSeq.map(_.unitClass).distinct.map(With.scheduler.dumbPumps.consumed).sum
    val capacityTotal         = buildersAvailable.size - buildersOccupied
    val capacityLeft          = buildCapacity - builders.map(_.unitClass).map(With.scheduler.dumbPumps.consumed).sum
    
    With.scheduler.request(
      this,
      RequestAtLeast(
        List(
          maximum,
          maxDesirable,
          Math.min(maximumConcurrently, buildCapacity) + currentCount
        ).min,
        unitClass))
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
