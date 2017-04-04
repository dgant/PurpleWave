package Lifecycle

object Manners {
  
  def onFrame() {
    if (With.configuration.enableSurrendering
      && With.self.supplyUsed == 0
      && With.minerals < 50
      && With.units.enemy.exists(_.unitClass.isWorker)
      && With.units.enemy.exists(_.unitClass.isResourceDepot)) {
      With.game.sendText("Good game! Let's pretend this never happened.")
      With.game.leaveGame()
    }
  }
  
  def onEnd(isWinner: Boolean) {
    With.logger.debug(
      if (isWinner)
        "Good game! I still think you're beautiful."
      else
        "Good game! Let's pretend this never happened.")
  }
}
