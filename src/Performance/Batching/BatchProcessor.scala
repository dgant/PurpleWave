package Performance.Batching

import Lifecycle.With

import scala.collection.mutable

abstract class BatchProcessor[TTask] {
  
  protected val taskItems:mutable.Queue[TTask] = mutable.Queue.empty
  
  final def run() {
    onRun()
    if (taskItems.isEmpty) {
      taskItems ++= onPopulate
    }
    while(taskItems.nonEmpty && With.performance.millisecondsLeftThisFrame > 0) {
      val nextItem = taskItems.dequeue()
      onNext(nextItem)
    }
  }
  
  protected def shouldRun():Boolean = true
  protected def onRun() {}
  protected def onPopulate():Traversable[TTask]
  protected def onNext(item:TTask)
}
