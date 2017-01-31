package Processes

import Startup.With
import Types.Requirements.Buyer

class Prioritizer {
  var _nextPriority:Integer = 0
  
  def startFrame() {
    _nextPriority = 0
    prioritize(With.gameplan)
    With.gameplan.children.foreach(prioritize)
  }
  
  def prioritize(buyer:Buyer) {
    buyer.priority = _nextPriority
    _nextPriority += 1
  }
}
