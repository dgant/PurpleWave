package Utilities

import scala.collection.mutable

/**
  * A syntax-light queue of things to do.
  * The intended use case is something you want to do exactly once,
  * but may want to consider doing at multiple points.
 */
class DoQueue(todo: (() => Unit)*) {
  val queue = new mutable.Queue[() => Unit]
  queue ++= todo

  def apply(): Unit = {
    if (queue.nonEmpty) {
      queue.dequeue()()
    }
  }
}
