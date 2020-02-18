package Debugging

import Lifecycle.{Manners, With}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KeyboardCommands {
  def quitVsHuman(): Unit = {
    if (With.configuration.humanMode) With.game.leaveGame()
  }

  def onSendText(text: String) {
    text match {
      case "q"          => breakpoint()
      case "c"          => With.camera.enabled            = ! With.camera.enabled
      case "v"          => With.visualization.enabled     = ! With.visualization.enabled
      case "vm"         => With.visualization.map         = ! With.visualization.map
      case "vs"         => With.visualization.screen      = ! With.visualization.screen
      case "vh"         => With.visualization.happy       = ! With.visualization.happy
      case "vt"         => With.visualization.textOnly    = ! With.visualization.textOnly

      case "1"          => With.game.setLocalSpeed(1000)  ; With.camera.enabled = false
      case "2"          => With.game.setLocalSpeed(60)    ; With.camera.enabled = false
      case "3"          => With.game.setLocalSpeed(30)    ; With.camera.enabled = false
      case "4"          => With.game.setLocalSpeed(0)     ; With.camera.enabled = false
      case "perform"    => { With.configuration.enablePerformancePauses = ! With.performance.enablePerformancePauses; Manners.chat("Performance stops? " + With.configuration.enablePerformancePauses) }
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
