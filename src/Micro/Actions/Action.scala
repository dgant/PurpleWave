package Micro.Actions

import Micro.Intent.Intention

trait Action {
  
  // Return true if the unit has executed a command, and is thus done acting.
  def perform(intent:Intention):Boolean
  
}
