package Performance.Tasks

class DummyTask extends TimedTask {
  var runFunction: () => Unit = () => {}
  override protected def onRun(budgetMs: Long): Unit = runFunction()
}
