package Lifecycle

import bwapi.{DefaultBWListener, Game, Text}

class BotScratch extends DefaultBWListener {

  var duration: Double = 0.0

  override def onFrame(): Unit = {
    val game: Game  = JBWAPIClient.getGame
    game.setScreenPosition(32 * 64, 32 * 64)
    game.setLocalSpeed(50)
    game.setTextSize(Text.Size.Large)
    game.drawTextScreen(300, 180, game.getFrameCount.toString)
    duration += 0.01
    Thread.sleep(duration.toInt)
  }

  override def onSendText(text: String): Unit = {
    duration = text.toDouble
  }
}
