package Information.Scouting

import Performance.Tasks.TimedTask

final class Scouting extends TimedTask
  with Debuts
  with EnemyTechs
  with EnemyScouting
  with BaseInference
  with Intrigue
  with Timings
  with Tugging {
  override protected def onRun(budgetMs: Long): Unit = {
    updateEnemyTechs()
    updateEnemyScouting()
    updateBaseInference()
    updateIntrigue()
    updateDebuts()
    updateTimings()
  }
}
