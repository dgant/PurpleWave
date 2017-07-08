package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAnother, RequestUnitAnotherOne}
import Planning.Plan
import ProxyBwapi.Races.Protoss

import scala.collection.mutable.ArrayBuffer

class TrainGatewayUnitsContinuously extends Plan {
  
  override def onUpdate() {
    
    val gateways = With.units.ours.filter(unit => unit.aliveAndComplete && unit.is(Protoss.Gateway))
    
    val requests = new ArrayBuffer[BuildRequest]
    
    gateways
      .filter(_.trainingQueue.nonEmpty)
      .map(_.trainingQueue.head)
      .foreach(unitInTraining => requests.append(RequestUnitAnotherOne(unitInTraining)))
    
    val capacity = Math.max(0, gateways.size - requests.size)
    val dragoons =
      if (With.units.ours.exists(unit => unit.aliveAndComplete && unit.is(Protoss.CyberneticsCore)))
        Math.min(capacity, With.self.gas / 50)
      else 0
    val zealots = Math.max(0, capacity - dragoons)
    requests.append(RequestAnother(dragoons,  Protoss.Dragoon))
    requests.append(RequestAnother(zealots,   Protoss.Zealot))
    
    With.scheduler.request(this, requests)
  }
}
