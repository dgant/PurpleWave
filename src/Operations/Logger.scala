package Operations

import Startup.BotListener

object Logger {
  def debug(message:String) {
    System.out.println(message)
    BotListener.bot.get.game.sendText(message)
  }
}
