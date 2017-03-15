package Planning.Plans.Macro.Automatic

import Startup.With

class TrainWorkersContinuously extends TrainContinuously {
  
  description.set("Build bases when we're about saturated")
  
  unitTypeToTrain = With.self.getRace.getWorker
  
  override protected def totalRequiredRecalculate:Int = {
    List(
      With.units.ours.count(_.utype.isWorker) + With.units.ours.count(_.utype == unitTypeToTrain.whatBuilds.first),
      With.economy.ourMiningBases.size * 24,
      72).min
  }
}
