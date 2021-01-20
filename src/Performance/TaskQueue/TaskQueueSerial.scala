package Performance.TaskQueue

import Performance.Tasks.TimedTask
import Performance.Timer

/**
  * Runs a group of tasks to be performed in order, and resumed when time expires
  */
class TaskQueueSerial(val tasks: TimedTask*) extends TimedTask {
  def this(name: String, t: TimedTask*) {
    this(t: _*)
    withName(name)
  }

  withAlwaysSafe(true)

  private var index: Int = 0

  override def isComplete: Boolean = framesSinceRunning < 1 && index == 0

  override def onRun(budgetMs: Long): Unit = {
    val timer = new Timer(budgetMs)
    var proceed = true
    while (proceed) {
      if (timer.expired) {
        proceed = false
      } else if (index >= tasks.length) {
        // We have reached the end of the task queue.
        // Start from the beginning next time.
        proceed = false
        index = 0
      } else {
        val task = tasks(index)
        proceed = task.safeToRun(timer.remaining)
        if (proceed) {
          task.run(timer.remaining)
          index += 1
          if ( ! task.isComplete) {
            // The task isn't complete,
            // but refused to keep running,
            // presumably because it exited for performance safety.
            // so we'll have to revisit it next time.
            proceed = false
          }
        }
      }
    }
  }
}
