package Processes

import Types.Buildable

class Scheduler {
  
  /*
    Plans request a queue of buildables
    Scheduler sorts by plan priority -> queue order
    
  
   */
  
  def queue:Iterable[Buildable] = {
    List.empty
  }
  
  def onFrame() {
    
  }
}
