package Processes

import Types.Tactics.Tactic

class Commander {
  def command(tactics: Iterable[Tactic]) {
    
    tactics.foreach(_.execute())
  }
}
