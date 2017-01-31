package Types.Plans

import Types.Tactics.Tactic

abstract class PlanParallel extends Plan {
  
  var _children:Iterable[Plan] = List.empty
  
  override def children(): Iterable[Plan] = {
    _requireInitialization()
    _children
  }
  
  override def startFrame() {
    super.startFrame()
    children.foreach(_.startFrame())
  }
  
  override def abort() {
    super.abort()
    children.foreach((_.abort()))
  }
  
  override def execute():Iterable[Tactic] = {
    children.flatten(_.execute)
  }
  
  override def active():Boolean = {
    ! isComplete()
  }
  
  override def isComplete():Boolean = {
    children.map(_.isComplete).foldLeft(true)(_&&_)
  }
}
