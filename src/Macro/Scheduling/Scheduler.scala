package Macro.Scheduling

import Macro.BuildRequests._
import Macro.Buildables.Buildable
import Macro.Scheduling.DumbQueue.{DumbPumps, DumbQueue}
import Macro.Scheduling.SmartQueue.SmartQueue
import Planning.Plan

class Scheduler {
  
  lazy val dumbPumps   = new DumbPumps
  lazy val dumbQueue   = new DumbQueue
  lazy val smartQueue  = new SmartQueue
  
  def reset() {
    dumbPumps.reset()
    dumbQueue.reset()
    //smartQueue.reset()
  }
  
  def request(requester: Plan, theRequest: BuildRequest) {
    request(requester, Iterable(theRequest))
  }
  
  def request(requester: Plan, requests: Iterable[BuildRequest]) {
    dumbQueue.request(requester, requests)
    //requests.foreach(smartQueue.enqueue)
  }
  
  def queue: Iterable[Buildable] = {
    dumbQueue.queue
  }
}
