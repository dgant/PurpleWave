package Planning.Plans.Macro.Automatic

import Lifecycle.With

class TrainWorkersContinuously(oversaturate: Boolean = false) extends TrainContinuously(With.self.workerClass) {
  
  override def maxDesirable: Int = Math.min(
    85,
    (if (oversaturate) 15 else 0) +
    /* Builders */  1 * With.geography.ourBases.size +
    /* Minerals */  3 * With.geography.ourBases.toVector.map(base => base.gas.size).sum +
    /* Gas      */  2 * With.geography.ourBases.toVector.map(base => base.minerals.size).sum)
}
