package Types.Plans

import Types.Requirements._
import Types.Tactics.Tactic
import Types.Traits.RequiresInitialization

abstract class Plan extends Buyer with RequiresInitialization {
  
  val _requirements:Requirement = new RequireNothing()
  var _isComplete:Boolean = false
 
  def prioritize() {
    
  }
  
  def children():Iterable[Plan] = {
    _requireInitialization()
    List.empty
  }
  
  def startFrame() {
    _requirements.fulfill()
  }
  
  def abort() {
    _requireInitialization()
    _requirements.abort()
  }
  
  def execute():Iterable[Tactic] = {
    _requireInitialization()
    List.empty
  }
  
  def active:Boolean = {
    _requireInitialization()
    _requirements.isFulfilled && ! isComplete
  }
  
  def isComplete():Boolean = {
    _requireInitialization()
    _isComplete
  }
  
  def _flagComplete() {
    _requireInitialization()
    _isComplete = true
    abort()
  }
  
  def _onInitialization() {
    _requirements.buyer = this
  }
}