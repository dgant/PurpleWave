package Plans.Macro.Automatic

import Startup.With

class BuildWorkersContinuously  extends TrainContinuously {
  
  description.set("Build bases when we're about saturated")
  
  unitType = With.game.self.getRace.getWorker
  
  override def _totalRequired:Int = {
    List(
      With.units.ours.count(_.utype.isWorker) + With.units.ours.count(_.utype == unitType.whatBuilds.first),
      With.economy.ourMiningBases.size * 24,
      72).min
  }
}
