package Planning.Plans.Macro.Automatic

import Macro.BuildRequests.{BuildRequest, RequestUnitAnother, RequestUnitAnotherOne}
import Planning.Plan
import ProxyBwapi.Races.Protoss
import Lifecycle.With

import scala.collection.mutable.ListBuffer

class TrainGatewayUnitsContinuously extends Plan {
  
  override def onFrame() {
    val gateways = With.units.ours.filter(u => u.alive && u.complete && u.unitClass == Protoss.Gateway)
    
    val requests = new ListBuffer[BuildRequest]
    
    gateways
      .filter(_.trainingQueue.nonEmpty)
      .map(_.trainingQueue.head)
      .foreach(unitInTraining => requests.append(new RequestUnitAnotherOne(unitInTraining)))
    
    val capacity = Math.max(0, gateways.size - requests.size)
    val dragoons =
      if (With.units.ours.exists(u => u.alive && u.complete && u.unitClass == Protoss.CyberneticsCore))
        Math.min(capacity, With.gas / 50)
      else 0
    val zealots   = Math.max(0, capacity - dragoons)
    requests.append(new RequestUnitAnother(dragoons, Protoss.Dragoon))
    requests.append(new RequestUnitAnother(zealots, Protoss.Zealot))
    
    With.scheduler.request(this, requests)
  }
}
