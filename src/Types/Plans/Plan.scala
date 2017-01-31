package Types.Plans

import Types.Requirements._
import Types.Tactics.Tactic
import Types.Traits.RequiresInitialization

abstract class Plan extends Buyer with RequiresInitialization {
  
  val requirements:Requirement = new RequireNothing()
  
  var _initialized:Boolean = false
  var isComplete:Boolean = false
  
  def _onInitialization() {
    requirements.buyer = this
  }
  
  def children():Iterable[Plan] = { return List.empty }
  def execute():Iterable[Tactic] = { return List.empty }
  
  def active:Boolean = { requirements.isFulfilled && ! isComplete }
  
  def abort() {
    requirements.abort()
  }
  
  def flagComplete() {
    isComplete = true
    abort()
  }
}