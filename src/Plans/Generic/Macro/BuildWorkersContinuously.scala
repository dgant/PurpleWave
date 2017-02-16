package Plans.Generic.Macro

import Plans.Plan
import Startup.With

import scala.collection.mutable.ListBuffer

class BuildWorkersContinuously extends Plan {
  var _currentBuilds = new ListBuffer[Plan]
  
  override def isComplete:Boolean = { getChildren.forall(_.isComplete) && _additionalWorkersDesired == 0 }
  override def getChildren: Iterable[Plan] = _currentBuilds
  
  override def onFrame() {
    _currentBuilds --= _currentBuilds.filter(_.isComplete)
    (1 to _additionalPlansRequired).foreach(i => _currentBuilds.append(_buildPlan))
    _currentBuilds.foreach(_.onFrame())
  }
  
  def _additionalPlansRequired:Int = {
    Math.max(0, _additionalWorkersDesired - _currentBuilds.size)
  }
  
  def _additionalWorkersDesired:Int = {
    Math.min(_workerCap - _workersNow, _maxWorkersToBuildSimultaneously)
  }
  
  def _workersNow:Int = {
    //Maybe we should use the Economist value, in case we're using workers for other things
    With.ourUnits.filter(_.getType.isWorker).size
  }
  
  def _workerCap:Int = {
    //Cap the number of bases to saturate so we don't accidentally max out on probes
    //Assuming we want three on gas and 2.5 per mineral
    Math.min(3, With.economist.ourMiningBases.size) * (3 + 9 * 3/2)
  }
  
  def _maxWorkersToBuildSimultaneously:Int = {
    Math.max(With.economist.ourMiningBases.size, 3 * _hatcheries)
  }
  
  def _hatcheries:Int = {
    With.ourUnits.filter(_.getType.producesLarva).size
  }
  
  def _buildPlan:Plan = {
    //This only builds workers of our own race. Sad!
    new TrainUnit(With.game.self.getRace.getWorker)
  }
}
