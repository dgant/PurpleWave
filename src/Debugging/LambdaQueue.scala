package Debugging

import scala.collection.mutable.ArrayBuffer

/**
  * For debugging.
  * Queue of lambdas to invoke on the next onFrame().
  *
  * This queue serves two purposes:
  * + Allow manually executionof code which hits breakpoints.
  * + Allow keyboard commands to execute changes which must happen during an onFrame() event.
  *
  * Example:
  *
  * // Somewhere in codebase:
  * someFunction() {
  *   // Set breakpoint here
  * }
  *
  * // Run from debug inspector:
  * With.lambdas.add(() => someFunction())
  */
class LambdaQueue {

  private val queue: ArrayBuffer[() => Unit] = new ArrayBuffer()

  def add(event: () => Unit): Unit = {
    queue += event
  }

  def onFrame(): Unit = {
    queue.foreach(_())
    queue.clear()
  }
}
