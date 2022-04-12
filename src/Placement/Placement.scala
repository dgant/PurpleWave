package Placement

import Performance.Tasks.TimedTask
import Placement.Generation.Generator

class Placement extends TimedTask with Generator {
  override protected def onRun(budgetMs: Long): Unit = {
    initialize()
  }
}
