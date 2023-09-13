package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Requests.Get
import Planning.{MacroFacts, Plan}
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.UnitFilters._

class Pump(
  unitClass           : UnitClass,
  maximumTotal        : Int = Int.MaxValue,
  maximumConcurrently : Int = Int.MaxValue)
    extends Plan {

  protected def maxDesirable: Int = Int.MaxValue

  val builderClass: UnitClass = unitClass.whatBuilds._1
  
  override def onUpdate(): Unit = {
    if ( ! canBuild) return
    val unitsComplete       = With.macroCounts.oursComplete(unitClass)
    val larvaSpawning       = if (builderClass == Zerg.Larva) With.units.countOurs(IsAll(IsHatchlike, IsComplete)) else 0
    val builders            = getBuildersExisting + larvaSpawning
    val unitsToAdd          = Seq(maximumConcurrently, 2 * builders * unitClass.copiesProduced).min
    val unitsToRequest      = Seq(maximumTotal, maxDesirable, unitsComplete + unitsToAdd).min
    With.scheduler.request(this, Get(unitsToRequest, unitClass))
  }

  final protected def canBuild: Boolean = (
    unitClass.buildTechEnabling.forall(MacroFacts.techStarted)
    && unitClass.buildUnitsEnabling.forall(With.units.existsOurs(_))
    && unitClass.buildUnitsBorrowed.forall(With.units.existsOurs(_)))

  final protected def getBuildersExisting: Int = With.units.ours.count(builder =>
    builderClass(builder)
    && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
    && ( ! unitClass.requiresPsi                                      || builder.powered)
    && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
    && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
    && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))

  override def toString: String = f"Pump $unitClass"
}
