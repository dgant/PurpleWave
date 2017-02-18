package Processes

import Plans.Plan
import Types.Buildable.Buildable

class Scheduler {
  
  /*
    Plans request a queue of buildables
    Scheduler sorts by plan priority -> queue order
    
  
   */
  
  def request(requester:Plan, buildable: Buildable) {
    
  }
  
  def queue:Iterable[Buildable] = {
    List.empty
  }
  
  def onFrame() {
    
  }
}
