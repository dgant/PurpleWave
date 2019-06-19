package Lifecycle

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Strategery.History.HistoryLoader

object Manners {
  
  def enabled: Boolean = With.configuration.enableChat
  
  def run() {
    if (With.configuration.enableSurrenders
      && With.self.supplyUsed == 0
      && With.self.minerals < 50
      && With.units.enemy.exists(_.unitClass.isWorker)
      && With.units.enemy.exists(_.unitClass.isResourceDepot)) {
      surrender()
    }
    if (With.configuration.enableSurrenders
      && With.performance.enablePerformanceStops
      && With.performance.enablePerformanceSurrenders
      && With.performance.framesOver55 > 1000) {
      With.logger.error("Quitting due to performance failure")
      surrender()
    }
    if (With.frame == GameTime(0, 20)()) {
      if (With.configuration.humanMode()) {
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
  
  def chat(text: String) {
    if (enabled) {
      With.game.sendText(text)
      //With.game.printf(text)
    }
  }
  
  def onEnd(isWinner: Boolean) {
    chat(
      if (With.configuration.humanMode())
        "Good game, " + With.enemy.name
      else if (isWinner)
        "Good game! I still think you're beautiful."
      else
        "Good game! Let's pretend this never happened.")
  }
}
