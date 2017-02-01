package Development

import Startup.With

object Logger {
  def debug(message:String) {
    System.out.println(message)
    With.game.sendText(message)
  }
}
