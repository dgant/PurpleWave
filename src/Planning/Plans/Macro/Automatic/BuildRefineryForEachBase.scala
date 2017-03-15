package Planning.Plans.Macro.Automatic

import Startup.With

class BuildRefineryForEachBase extends TrainContinuously {
  
  description.set("Build a refinery for each base")
  
  unitTypeToTrain = With.self.getRace.getRefinery
  
  override protected def totalRequiredRecalculate:Int = With.economy.ourMiningBases.size
}
