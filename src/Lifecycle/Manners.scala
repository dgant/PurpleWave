package Lifecycle

import Information.Intelligenze.Fingerprinting.Generic.GameTime

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
    if (With.configuration.enableHistoryChat && With.frame == GameTime(0, 20)()) {
      With.history.message.foreach(chat)
    }
  }
  
  private def surrender() {
    With.game.leaveGame()
  }
  
  def chat(text: String) {
    if (enabled) {
      //With.game.sendText(text)
      With.game.printf(text)
    }
  }
  
  def onEnd(isWinner: Boolean) {
    chat(
      if (isWinner)
        "Good game! I still think you're beautiful."
      else
        "Good game! Let's pretend this never happened.")
  }
}
