package Performance.Tasks

class SimpleTask(lambda: () => Unit) extends TimedTask {
  def this(name: String, lambda: () => Unit) {
    this(lambda)
    withName(name)
  }
  override def onRun(budgetMs: Long): Unit = lambda()
}
