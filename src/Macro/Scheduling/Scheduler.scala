package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests._
import Macro.Buildables.Buildable
import Planning.Plan
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.{ByOption, CountMap}

class Scheduler {

  val unitsRequested: CountMap[UnitClass] = new CountMap[UnitClass]
  val macroQueue = new MacroQueue
  
  def reset() {
    unitsRequested.clear()
    macroQueue.reset()
  }
  
  def request(requester: Plan, theRequest: BuildRequest) {
    macroQueue.request(requester, theRequest)
    theRequest.buildable.unitOption.foreach(unit => {
      unitsRequested(unit) = Math.max(unitsRequested(unit), theRequest.require)
      unitsRequested(unit) = Math.max(unitsRequested(unit), With.units.countOurs(unit)) + theRequest.add
    })
  }

  def buildersConsumed(unit: UnitClass): Int = {
    val ratio = if (unit.isTwoUnitsInOneEgg) 2 else 1
    ByOption.min((unit.buildUnitsBorrowed ++ unit.buildUnitsSpent).map(builder => {
      val unitsNow        = With.units.countOurs(UnitMatchAnd(unit, UnitMatchComplete))
      val unitsRequested  = builder.unitsTrained.toVector.map(u => Math.max(0, this.unitsRequested(u) - With.units.countOurs(UnitMatchAnd(u, UnitMatchComplete)))).sum
      val unitsToBuild    = Math.max(0, unitsRequested - unitsNow)
      val buildersUsed    = unitsToBuild / ratio
      buildersUsed
    })).getOrElse(0)
  }
  
  def queue: Iterable[Buildable] = {
    macroQueue.queue
  }
}
