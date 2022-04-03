package Information.Scouting

import Performance.Tasks.TimedTask

final class Scouting extends TimedTask with EnemyTechs with EnemyScouting with BaseInference with Tugging with Intrigue {
  override protected def onRun(budgetMs: Long): Unit = {
    updateEnemyTechs()
    updateEnemyScouting()
    updateBaseInference()
    updateIntrigue()
  }
}
