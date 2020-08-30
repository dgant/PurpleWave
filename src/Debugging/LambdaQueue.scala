package Debugging

import scala.collection.mutable.ArrayBuffer

/**
  * For debugging.
  * Queue of lambdas to invoke on the next onFrame().
  * The purpose of this is to allow manual execution of code which hits breakpoints. Example:
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

  }

  def update(): Unit = {
    queue.foreach(_)
    queue.clear()
  }
}
