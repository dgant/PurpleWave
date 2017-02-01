package Processes

import Types.Tactics.Tactic

import scala.collection.mutable.ListBuffer

class Commander {
  
  val _tactics = new ListBuffer[Tactic]
  
  def queue(tactic:Tactic) {
    _tactics.append(tactic)
  }
  
  def execute() {
    _tactics.foreach(_.execute())
    _tactics.clear()
  }
}
