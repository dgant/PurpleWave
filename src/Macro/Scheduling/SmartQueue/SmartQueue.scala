package Macro.Scheduling.SmartQueue

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Macro.Buildables.Buildable
import Mathematics.PurpleMath

class SmartQueue {
  
  var queue: Iterable[Buildable] = Iterable.empty

  private var queueState: SmartQueueState = new SmartQueueState
  
  def reset() {
    queueState = new SmartQueueState
  }
  
  def enqueue(buildRequests: BuildRequest*) {
    buildRequests.foreach(enqueue)
  }
  
  def enqueue(buildRequest: BuildRequest) {
    var quantityNow = 0
    quantityNow += buildRequest.buildable.unitOption.map(queueState.unitsNow).getOrElse(0)
    quantityNow += buildRequest.buildable.upgradeOption.map(With.self.getUpgradeLevel).getOrElse(0)
    quantityNow += buildRequest.buildable.techOption.map(With.self.hasTech).map(PurpleMath.fromBoolean).getOrElse(0)
    val quantityToAdd = Math.max(buildRequest.add, buildRequest.require - quantityNow)
    (1 to quantityToAdd).foreach(i => enqueueOnce(buildRequest))
  }
  
  private def enqueueOnce(buildRequest: BuildRequest) {
    queueState.input += new SmartQueueItem(add = Some(buildRequest))
  }
  
  def pump(buildRequest: BuildRequest, maximumTotal: Int, maximumSimultaneous: Int) {
    queueState.input += new SmartQueueItem(pump = Some(buildRequest))
  }
  
  def dump(buildRequest: BuildRequest) {
    queueState.input += new SmartQueueItem(dump = Some(buildRequest))
  }
  
  def publish() {
    // Is this right?!
    queue = queueState.output.map(_.buildable)
  }
}
