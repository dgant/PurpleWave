package Planning.Plans.Macro.Automatic

import Lifecycle.With

class PumpWorkers(oversaturate: Boolean = false, maximumTotal: Int = 75, maximumConcurrently: Int = 2) extends Pump(With.self.workerClass, maximumTotal = maximumTotal, maximumConcurrently = maximumConcurrently) {
  
  protected def builderCount: Int = {
    if (With.self.isTerran)       3
    else if (With.self.isProtoss) 0
    else                          2
  }
  override def maxDesirable: Int = {
    var sum = (if (oversaturate) 15 else 0) // 6 less than 21; one town hall can produce 6 workers in the time it takes to finish a new town hall
    With.geography.ourBases.foreach(base => {
      sum += builderCount + 2 * base.minerals.count(_.resourcesLeft > 200) + base.gas.view.filter(_.resourcesLeft > 200).map(_.gasMinersRequired).sum
    })
    Math.max(sum, 21)
  }
}
