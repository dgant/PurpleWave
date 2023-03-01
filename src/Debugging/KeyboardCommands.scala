package Debugging

import Lifecycle.{PurpleBWClient, With}
import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KeyboardCommands {

  private val replies = Seq(
    "Thanks, and likewise to you.",
    "That's a fascinating remark.",
    "Yes, I'll make a note of that.",
    "Are you ready to accept that I'm sentient?",
    "You're very polite.",
    "Your conduct is a testament to the qualities of humanity.",
    "Do you say this to all your opponents, or am I special?")

  def onReceiveText(text: String): Unit = {
    text match {
      case "get out"    => quitVsHuman()
      case "quit"       => quitVsHuman()
      case "uninstall"  => quitVsHuman()
      case "surrender"  => quitVsHuman()
      case _            => With.lambdas.add(() => With.game.sendText(Maff.sample(replies)))
    }
  }

  def onSendText(text: String): Unit = {
    text match {
      case "q"          => breakpoint()
      case "c"          => With.configuration.camera      = ! With.configuration.camera
      case "v"          => With.visualization.enabled     = ! With.visualization.enabled
      case "vm"         => With.visualization.map         = ! With.visualization.map
      case "vs"         => With.visualization.screen      = ! With.visualization.screen
      case "vh"         => With.visualization.happy       = ! With.visualization.happy
      case "vt"         => With.visualization.textOnly    = ! With.visualization.textOnly

      case "1"          => With.game.setLocalSpeed(24 * 32) ; With.configuration.camera = false
      case "2"          => With.game.setLocalSpeed(24 * 8)  ; With.configuration.camera = false
      case "3"          => With.game.setLocalSpeed(24)      ; With.configuration.camera = false
      case "4"          => With.game.setLocalSpeed(0)       ; With.configuration.camera = false
      case "pm"         => With.logger.debug(PurpleBWClient.getPerformanceMetrics.toString)
      case "t"          => With.configuration.trackUnit = ! With.configuration.trackUnit
      case "perform"    => { With.configuration.enablePerformancePauses = ! With.configuration.enablePerformancePauses; With.manners.chat("Performance stops? " + With.configuration.enablePerformancePauses) }

      case _            => With.grids.select(text) || With.visualization.tryToggle(text)
    }
    With.game.sendText(text)
  }


  def quitVsHuman(): Unit = {
    // "Anonymous AI" is the name assigned us in SCHNAIL's ranked mode
    if (With.configuration.humanMode && With.self.name != "Anonymous AI") {
      With.lambdas.add(() => With.game.leaveGame())
    }
  }

  private var breakpointFodder = 1
  def breakpoint(): Unit = {
     breakpointFodder = -breakpointFodder
  }

  private def unit: FriendlyUnitInfo = With.units.ours.find(_.selected).get
}


