package Macro.Allocation

import Lifecycle.With

import scala.collection.mutable

class Priorities {
  private var _lastResetFrame: Int = -1
  private var _nextPriority: Int = 0

  val frameDelays: mutable.Queue[Int] = new mutable.Queue[Int]

  def lastResetFrame: Int = _lastResetFrame

  def nextPriority(): Int = {
    _nextPriority += 1
    _nextPriority - 1
  }
  
  def update() {
    _lastResetFrame = With.frame
    _nextPriority = 0
    frameDelays.enqueue(With.framesSince(_lastResetFrame))
    while(frameDelays.sum > 24 * 10) {
      frameDelays.dequeue()
    }
  }
}
