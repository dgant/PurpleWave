package Plans.Macro.Automatic

import Plans.Macro.Build.TrainUnit
import Startup.With

class BuildWorkersContinuously extends AbstractBuildContinuously[TrainUnit] {
  
  override def _createPlan:TrainUnit = {
    //This only builds workers of our own race. Sad!
    new TrainUnit(With.game.self.getRace.getWorker)
  }
  
  override def _additionalPlansRequired:Int = {
    Math.max(0, _additionalWorkersDesired - _currentBuilds.size)
  }
  
  def _additionalWorkersDesired:Int = {
    Math.min(_workerCap - _workersNow, _maxWorkersToBuildSimultaneously)
  }
  
  def _workersNow:Int = {
    With.economy.ourActiveHarvesters.size
  }
  
  def _workerCap:Int = {
    Math.min(3, With.economy.ourMiningBases.size) * 24
  }
  
  def _maxWorkersToBuildSimultaneously:Int = {
    Math.max(With.economy.ourMiningBases.size, 3 * _hatcheries)
  }
  
  def _hatcheries:Int = {
    With.units.ours.filter(_.utype.producesLarva).size
  }
}
