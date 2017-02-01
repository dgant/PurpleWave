package Processes

import Types.Tactics.Tactic

import scala.collection.mutable.ListBuffer

class Commander {
  
  val _tactics = new ListBuffer[Tactic]
  
  def clearQueue() {
    _tactics.clear()
  }
  
  def getQueue():Iterable[Tactic] = {
    _tactics
  }
  
  def enqueue(tactic:Tactic) {
    _tactics.append(tactic)
  }
  
  def execute() {
    _tactics.foreach(_.execute())
    
  }
}
