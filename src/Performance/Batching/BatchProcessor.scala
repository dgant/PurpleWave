package Performance.Batching

import Lifecycle.With

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class BatchProcessor[TInput, TOutput] {
  
  protected val inputs:mutable.Queue[TInput] = mutable.Queue.empty
  
  private var lastBatchOutputs = new ArrayBuffer[TOutput]
  private var nextBatchOutputs = new ArrayBuffer[TOutput]
  protected def lastBatch:ArrayBuffer[TOutput] = lastBatchOutputs
  
  final def run() {
    onBatchRun()
    if (inputs.isEmpty) {
      onBatchComplete()
      inputs ++= queueBatch
      nextBatchOutputs = new ArrayBuffer[TOutput]
    }
    while(inputs.nonEmpty && With.performance.millisecondsLeftThisFrame > 0) {
      val nextInput = inputs.dequeue()
      val nextOutput = onBatchProcess(nextInput)
      nextBatchOutputs.append(nextOutput)
    }
  }
  
  /////////////////
  // Batch steps //
  /////////////////
  
  protected def shouldBatchRun():Boolean = true
  protected def onBatchRun() {}
  protected def queueBatch():Traversable[TInput]
  protected def onBatchComplete()
  protected def onBatchProcess(item:TInput): TOutput
}
