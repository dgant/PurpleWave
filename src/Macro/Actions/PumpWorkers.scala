package Macro.Actions

import Lifecycle.With
import Utilities.?

object PumpWorkers extends MacroActions {

  def apply(oversaturate: Boolean = false, maximumTotal: Int = 75, maximumConcurrently: Int = 2): Unit = {
    val builderCount = ?(With.self.isTerran, 3, ?(With.self.isZerg, 2, 0))
    var max = ?(oversaturate, 15, 0) // 6 less than 21; one town hall can produce 6 workers in the time it takes to finish a new town hall
    With.geography.ourBases.foreach(base =>
      max += builderCount + 2 * base.minerals.count(_.resourcesLeft > 200) + base.gas.view.filter(_.resourcesLeft > 200).map(_.gasMinersRequired).sum)
    max = Math.max(max, 21)
    pump(With.self.workerClass, max, maximumConcurrently)
  }
}
