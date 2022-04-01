package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Buildables.Get
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
    val unitsComplete       = With.macroCounts.oursComplete(unitClass)
    val unitsExtant         = With.macroCounts.oursExtant(unitClass)
    val larvaSpawning       = if (builderClass == Zerg.Larva) With.units.countOurs(MatchAnd(MatchHatchlike, MatchComplete)) else 0
    val builders            = getBuildersExisting.size + larvaSpawning
    val unitsToAdd          = Seq(maximumConcurrently, 3 * builders * unitClass.copiesProduced).min
    val unitsToRequest      = Seq(maximumTotal, maxDesirable, unitsExtant + unitsToAdd).min
    With.scheduler.request(this, Get(unitsToRequest, unitClass))
  }

  final protected def canBuild: Boolean = (
    unitClass.buildTechEnabling.forall(With.self.hasTech)
    && unitClass.buildUnitsEnabling.forall(With.units.existsOurs(_))
    && unitClass.buildUnitsBorrowed.forall(With.units.existsOurs(_)))

  final protected def getBuildersExisting: Iterable[FriendlyUnitInfo] = With.units.ours
    .view
    .filter(builder =>
      builder.alive
        && builderClass(builder)
        && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
        && ( ! unitClass.requiresPsi                                      || builder.powered)
        && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))

  override def toString: String = f"Pump $unitClass"
}
