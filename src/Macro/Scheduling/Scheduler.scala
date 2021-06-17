package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests._
import Mathematics.Maff
import Planning.Plan
import Planning.UnitMatchers.{MatchAnd, MatchComplete}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

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
      unitsRequested(unit) = Math.max(unitsRequested(unit), theRequest.total)
    })
  }

  def buildersAllocated(unit: UnitClass): Int = {
    Maff.max((unit.buildUnitsBorrowed ++ unit.buildUnitsSpent).map(builder => {
      val traineesNow       = builder.unitsTrained.toVector.map(u => With.units.countOurs(MatchAnd(u, MatchComplete))).sum
      val traineesRequested = builder.unitsTrained.toVector.map(u => Math.max(0, this.unitsRequested(u) - With.units.countOurs(MatchAnd(u, MatchComplete)))).sum
      val traineesToBuild   = Math.max(0, traineesRequested - traineesNow)
      val buildersAllocated = traineesToBuild / unit.copiesProduced
      buildersAllocated
    })).getOrElse(0)
  }
}
