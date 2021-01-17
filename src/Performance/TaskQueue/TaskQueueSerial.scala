package Performance.TaskQueue

import Performance.Tasks.TimedTask

/**
  * Runs a group of tasks to be performed in order, and resumed when time expires
  */
class TaskQueueSerial(getTasks: () => Seq[TimedTask]) extends TimedTask {
  def this(name: String, tasks: () => Seq[TimedTask]) {
    this(tasks)
    withName(name)
  }
  def this(fixedTasks: TimedTask*) {
    this(() => fixedTasks)
  }
  def this(name: String, fixedTasks: TimedTask*) {
    this(() => fixedTasks)
    withName(name)
  }

  withAlwaysSafe(true)

  private var index: Int = 0

  override def isComplete: Boolean = framesSinceRunning < 1 && index == 0

  override def onRun(): Unit = {
    val tasks = getTasks()
    var proceed = true
    while (proceed) {
      if (index >= tasks.length) {
        // We have reached the end of the task queue
        // Start from the beginning next time
        proceed = false
        index = 0
      } else {
        val task = tasks(index)
        proceed = task.safeToRun
        if (proceed) {
          task.run()
          index += 1
        }
        if (!task.isComplete) {
          // The task isn't complete,
          // but refused to keep running,
          // presumably because it exited for performance safety.
          // So we'll have to revisit it next time
          proceed = false
        }
      }
    }
  }
}
