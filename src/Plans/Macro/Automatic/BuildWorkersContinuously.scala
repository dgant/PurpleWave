package Plans.Macro.Automatic

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}

class BuildWorkersContinuously extends AbstractBuildContinuously {
  
  override def _newBuild:Buildable = new BuildableUnit(With.game.self.getRace.getWorker)
  override def _buildsRequired:Int = Math.max(0, _workerCap - _workersNow)
  def _workersNow:Int = With.units.ours.filter(_.utype.isWorker).size
  def _workerCap:Int = Math.min(3, With.economy.ourMiningBases.size) * 24
}
