package Performance.Tasks

class DummyTask(classType: Class[_]) extends TimedTask {
  withName(if (classType.getSimpleName.contains("anon")) classType.getSuperclass.getSimpleName else classType.getSimpleName)
  var runFunction: () => Unit = () => {}
  override protected def onRun(budgetMs: Long): Unit = runFunction()
}
