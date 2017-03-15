package Planning.Plans.Macro.Automatic

import Startup.With

class BuildExpansionWhenSaturated extends TrainContinuously {
  
  description.set("Build bases when we're about saturated")
  
  unitTypeToTrain = With.self.getRace.getCenter
  
  override protected def totalRequiredRecalculate:Int = 1 + With.units.ours.count(_.utype.isWorker) / 20
}
