package Lifecycle

import Performance.Tasks.TimedTask
import Utilities.UnitFilters.IsWorker
import Utilities.Time.Seconds

class Manners extends TimedTask {
  
  def enabled: Boolean = With.configuration.enableChat
  
  override def onRun(budgetMs: Long) {
    if (With.configuration.enableSurrenders
      && With.self.supplyUsed400 == 0
      && With.self.minerals < 50
      && With.units.existsEnemy(IsWorker)
      && With.units.existsEnemy(_.unitClass.isResourceDepot)) {
      surrender()
    }
    if (With.frame == Seconds(5)()) {
      if (With.configuration.humanMode) {
        chat("Good luck, " + With.enemy.name + ", and have fun!")
      }
      else {
        With.history.message.foreach(chat)
      }
    }
  }
  
  private def surrender() {
    chat("Well played.")
    With.game.leaveGame()
  }

  def debugChat(text: String): Unit = {
    if (With.configuration.debugging) {
      chat(text)
    }
  }
  
  def chat(text: String) {
    if (enabled) {
      With.game.sendText(text)
      //With.game.printf(text)
    }
  }
  
  def onEnd(isWinner: Boolean) {
    chat(
      if (With.configuration.humanMode)
        "Good game, " + With.enemy.name
      else if (isWinner)
        "Good game! I still think you're beautiful."
      else
        "Good game! Let's pretend this never happened.")
  }
}
