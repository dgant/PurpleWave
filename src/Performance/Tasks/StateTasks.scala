package Performance.Tasks

import scala.collection.mutable

class StateTasks {
  private val tasks = new mutable.HashMap[Class[_], DummyTask]()
  def get(state: Any): DummyTask = {
    val stateClass = state.getClass
    if ( ! tasks.contains(stateClass)) {
      tasks(stateClass) = new DummyTask(stateClass)
    }
    tasks(stateClass)
  }

  def safeToRun(state: Any, budgetMs: Long): Boolean = {
    get(state).safeToRun(budgetMs)
  }

  def run(state: Any, runFunction: () => Unit, budgetMs: Long): Unit = {
    val task = get(state)
    task.runFunction = runFunction
    task.run(budgetMs)
  }
}
