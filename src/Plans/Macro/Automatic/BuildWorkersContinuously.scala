package Plans.Macro.Automatic

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}

class BuildWorkersContinuously extends AbstractBuildContinuously {
  
  override def _newBuild:Buildable = new BuildableUnit(With.game.self.getRace.getWorker)
  override def _totalRequired:Int = Math.min(
    _workersNow + With.units.ours
      .filter(unit => unit.trainingQueue.size == 0 || unit.trainingQueue.size == 1 && unit.framesBeforeBuildeeComplete < With.game.getLatencyFrames)
      .count(_.utype == With.game.self.getRace.getWorker.whatBuilds.first),
    Math.max(0, _workerCap - _workersNow))
  
  def _workersNow:Int = With.units.ours.filter(_.utype.isWorker).size
  def _workerCap:Int = Math.min(3, With.economy.ourMiningBases.size) * 24
}
