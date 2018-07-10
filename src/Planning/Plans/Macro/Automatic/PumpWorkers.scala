package Planning.Plans.Macro.Automatic

import Lifecycle.With

class PumpWorkers(oversaturate: Boolean = false, cap: Int = 85) extends Pump(With.self.workerClass) {
  
  protected def builderCount: Int = {
    if (With.self.isTerran)
      4
    else if (With.self.isProtoss)
      2
    else
      3
  }
  override def maxDesirable: Int = Math.min(
    cap,
    (if (oversaturate) 18 else 0) +
    /* Builders */  builderCount * With.geography.ourBases.size +
    /* Minerals */  3 * With.geography.ourBases.toVector.map(base => base.gas.size).sum +
    /* Gas      */  2 * With.geography.ourBases.toVector.map(base => base.minerals.size).sum)
}
