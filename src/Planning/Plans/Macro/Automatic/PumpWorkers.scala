package Planning.Plans.Macro.Automatic

import Lifecycle.With

class PumpWorkers(oversaturate: Boolean = false, cap: Int = 85, maximumConcurrently: Int = 200) extends Pump(With.self.workerClass, maximumConcurrently = maximumConcurrently) {
  
  protected def builderCount: Int = {
    if (With.self.isTerran)
      4
    else if (With.self.isProtoss)
      1
    else
      2
  }
  override def maxDesirable: Int = Math.min(
    cap,
    {
      var sum = (if (oversaturate) 18 else 0)
      With.geography.ourBases.foreach(base => {
        sum += builderCount + 2 * base.minerals.count(_.resourcesLeft > 500) + 3 * base.gas.count(_.resourcesLeft > 500)
      })
      sum = Math.max(sum, 21)
      sum
    })
}
