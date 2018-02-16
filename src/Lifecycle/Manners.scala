package Lifecycle

object Manners {
  
  def enabled: Boolean = With.configuration.enableChat
  
  def run() {
    if (With.configuration.enableSurrendering
      && With.self.supplyUsed == 0
      && With.self.minerals < 50
      && With.units.enemy.exists(_.unitClass.isWorker)
      && With.units.enemy.exists(_.unitClass.isResourceDepot)) {
      surrender()
    }
    if (With.configuration.enableSurrendering && With.performance.framesOver85 > 1000) {
      With.logger.error("Quitting due to performance failure")
      surrender()
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
