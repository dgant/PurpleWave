package Macro.Scheduling

import Lifecycle.With
import Macro.BuildRequests._
import Macro.Buildables.Buildable
import Planning.Plan
import Planning.Plans.Macro.Automatic.PumpCount

class Scheduler {
  
  lazy val macroPumps = new MacroPumps
  lazy val macroQueue = new MacroQueue
  
  def reset() {
    macroPumps.reset()
    macroQueue.reset()
  }
  
  def request(requester: Plan, theRequest: BuildRequest) {
    request(requester, Iterable(theRequest))
  }

  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    requests.foreach(request =>
      request.buildable.unitOption.foreach(unitClass => {
        val unitsToProduce        = Math.max(0, request.add + request.require - PumpCount.currentCount(unitClass))
        val builderMultiplier     = if (unitClass.isTwoUnitsInOneEgg) 2 else 1
        val buildersToConsume     = (1 + request.add) / builderMultiplier
        val buildersToConsumeUpTo = (1 + request.require) / builderMultiplier
        val buildersOccupied      = unitClass.buildUnitsBorrowed ++ unitClass.buildUnitsSpent
        buildersOccupied.foreach(builderClass => {
          With.scheduler.macroPumps.consume(builderClass, buildersToConsume)
          With.scheduler.macroPumps.consumeUpTo(builderClass, buildersToConsumeUpTo)
        })
        With.scheduler.macroPumps.pump(unitClass, request.add)
        With.scheduler.macroPumps.buildUpTo(unitClass, request.require)
      }))

    macroQueue.request(requester, requests)
  }
  
  def queue: Iterable[Buildable] = {
    macroQueue.queue
  }
}
