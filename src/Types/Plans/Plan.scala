package Types.Plans

import Types.Requirements._
import Types.Tactics.Tactic

abstract class Plan extends Buyer {
  
  val requirements:Requirement = new RequireNothing()
  
  var _initialized:Boolean = false
  
  def initialize() {
    if ( ! _initialized) {
      _onInitialization()
      _initialized = true;
    }
  }
  
  def _onInitialization() {
    requirements.buyer = this
    requirements.priorityMultiplier = PriorityMinimum
  }
  
  def update() = {}
  def children():Iterable[Plan] = { return List.empty }
  def execute():Iterable[Tactic] = { return List.empty }
  
  def active:Boolean = { requirements.isFulfilled }
  
  def abort() {
    requirements.abort()
  }
}