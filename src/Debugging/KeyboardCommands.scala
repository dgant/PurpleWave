package Debugging

import Lifecycle.{Manners, With}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KeyboardCommands {
  def quitVsHuman(): Unit = {
    if (With.configuration.humanMode()) With.game.leaveGame()
  }

  def onSendText(text: String) {
    text match {
      case "q"          => breakpoint()
      case "c"          => With.configuration.camera      = ! With.configuration.camera
      case "v"          => With.visualization.enabled     = ! With.visualization.enabled
      case "vm"         => With.visualization.map         = ! With.visualization.map
      case "vs"         => With.visualization.screen      = ! With.visualization.screen
      case "vh"         => With.visualization.happy       = ! With.visualization.happy
      case "vt"         => With.visualization.textOnly    = ! With.visualization.textOnly

      case "1"          => With.game.setLocalSpeed(1000)  ; With.configuration.camera = false
      case "2"          => With.game.setLocalSpeed(60)    ; With.configuration.camera = false
      case "3"          => With.game.setLocalSpeed(30)    ; With.configuration.camera = false
      case "4"          => With.game.setLocalSpeed(0)     ; With.configuration.camera = false
      case "d"          => With.configuration.doAbsolutelyNothing = With.configuration.doAbsolutelyNothing
      case "perform"    => { With.performance.enablePerformanceStops = ! With.performance.enablePerformanceStops; Manners.chat("Performance stops? " + With.performance.enablePerformanceStops) }
      case "map"        => Manners.chat("The current map is " + With.game.mapName + ": " + With.game.mapFileName)
      case "strategize" => With.strategy.selectInitialStrategies

      case "get out"    => quitVsHuman()
      case "quit"       => quitVsHuman()
      case "uninstall"  => quitVsHuman()
      case "surrender"  => quitVsHuman()
      case _            => With.visualization.tryToggle(text)
    }
  }

  private var breakpointFodder = 1
  def breakpoint() {
    breakpointFodder = -breakpointFodder
  }

  def selected: FriendlyUnitInfo = With.units.ours.find(_.selected).get
}
