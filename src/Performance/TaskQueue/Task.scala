package Performance.TaskQueue

trait Task {
  def canRun: Boolean
  def run(): Unit
  def skip(): Unit
}
