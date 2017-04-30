package Micro.Actions

import Micro.Intent.Intention

abstract class Action {
  
  def allowed(intent:Intention): Boolean
  
  // Return true if the unit has executed a command, and is thus done acting.
  def perform(intent:Intention): Boolean
 
  val name = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  def consider(intent:Intention): Boolean = {
    if (allowed(intent)) return perform(intent)
    false
  }
}
