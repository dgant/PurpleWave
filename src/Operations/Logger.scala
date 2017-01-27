package Operations

import Startup.Main

object Logger {
  def debug(message:String) {
    System.out.println(message)
    Main.bot.get.game.sendText(message)
  }
}
