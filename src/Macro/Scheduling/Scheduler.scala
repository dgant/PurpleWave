package Macro.Scheduling

import Macro.BuildRequests._
import Macro.Buildables.Buildable

import Planning.Plan

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
    macroQueue.request(requester, requests)
  }
  
  def queue: Iterable[Buildable] = {
    macroQueue.queue
  }
}
