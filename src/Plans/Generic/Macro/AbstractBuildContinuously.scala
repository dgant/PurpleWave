package Plans.Generic.Macro

import Plans.Plan

import scala.collection.mutable.ListBuffer

abstract class AbstractBuildContinuously extends Plan {
  
  val _currentBuilds = new ListBuffer[Plan]
  
  override def isComplete:Boolean = { getChildren.forall(_.isComplete) && _additionalPlansRequired == 0 }
  override def getChildren: Iterable[Plan] = _currentBuilds
  
  override def onFrame() {
    _currentBuilds --= _currentBuilds.filter(_.isComplete)
    (1 to _additionalPlansRequired).foreach(i => _currentBuilds.append(_buildPlan))
    _currentBuilds.foreach(_.onFrame())
  }
  
  def _additionalPlansRequired:Int
  def _buildPlan:Plan
}
