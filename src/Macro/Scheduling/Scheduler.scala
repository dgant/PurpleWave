package Macro.Scheduling

import Macro.BuildRequests._
import Macro.Buildables.Buildable
import Macro.Scheduling.DumbQueue.DumbQueue
import Macro.Scheduling.SmartQueue.SmartQueue
import Planning.Plan

class Scheduler {
  
  lazy val dumbQueue   = new DumbQueue
  lazy val smartQueue  = new SmartQueue
  
  private val useSmartQueue = false
  
  def reset() {
    dumbQueue.reset()
    smartQueue.reset()
  }
  
  def request(requester: Plan, theRequest: BuildRequest) {
    request(requester, Iterable(theRequest))
  }
  
  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    dumbQueue.request(requester, requests)
    requests.foreach(smartQueue.enqueue)
  }
  
  def queue: Iterable[Buildable] = {
    dumbQueue.queue
    /*
    if (useSmartQueue) {
      smartQueue.publish()
    }
    else {
      dumbQueue.queue
    }
    */
  }
}
