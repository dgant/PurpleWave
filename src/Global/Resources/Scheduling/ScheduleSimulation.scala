package Global.Resources.Scheduling

import Types.Buildable.Buildable
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.mutable

class ScheduleSimulation(
  var frame               : Int,
  var minerals            : Int,
  var gas                 : Int,
  var supplyAvailable     : Int,
  var unitsOwned          : mutable.HashMap[UnitType, Int],
  var unitsAvailable      : mutable.HashMap[UnitType, Int],
  var techsOwned          : mutable.Set[TechType],
  var upgradeLevels       : mutable.Map[UpgradeType, Int]) {
  
  val events:mutable.PriorityQueue[SimulationEvent] = new mutable.PriorityQueue
  
  def enqueueEvent(event:SimulationEvent) {
    events.enqueue(event)
  }
  
  def dequeueEvent() {
    if (events.isEmpty) throw new Exception("There's no next event to simulate")
    val event = events.dequeue()
    frame = event.frameEnd
    finishBuilding(event.buildable)
  }
  
  def startBuilding(buildable:Buildable) {
    minerals -= buildable.minerals
    gas -= buildable.gas
    supplyAvailable -= buildable.supplyRequired
    buildable.buildersOccupied.foreach(builder => {
      unitsAvailable(builder.unit) -= 1
      events.enqueue(newCompletionEvent(builder))
      //TODO: Don't add builder back if it's consumed
    })
    buildable.buildersConsumed.foreach(builder => {
      unitsOwned(builder.unit) -= 1
    })
    events.enqueue(newCompletionEvent(buildable))
  }
  
  def finishBuilding(buildable: Buildable) {
    supplyAvailable += buildable.supplyProvided
    buildable.unitOption.foreach(unit => unitsOwned(unit) += 1)
  }
  
  def newCompletionEvent(buildable:Buildable):SimulationEvent = {
    new SimulationEvent(frame + buildable.frames, buildable)
  }
}