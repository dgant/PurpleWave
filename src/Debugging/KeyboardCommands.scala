package Debugging

import Lifecycle.{JBWAPIClient, With}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KeyboardCommands {
  def quitVsHuman(): Unit = {
    if (With.configuration.humanMode) With.lambdas.add(() => With.game.leaveGame())
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

      case "1"          => With.game.setLocalSpeed(24 * 32) ; With.configuration.camera = false
      case "2"          => With.game.setLocalSpeed(24 * 8)  ; With.configuration.camera = false
      case "3"          => With.game.setLocalSpeed(24)      ; With.configuration.camera = false
      case "4"          => With.game.setLocalSpeed(0)       ; With.configuration.camera = false
      case "perform"    => { With.configuration.enablePerformancePauses = ! With.configuration.enablePerformancePauses; With.manners.chat("Performance stops? " + With.configuration.enablePerformancePauses) }
      case "map"        => With.logger.debug("The current map is " + With.game.mapName + ": " + With.game.mapFileName)
      case "pm"         => With.logger.debug(JBWAPIClient.getPerformanceMetrics.toString)
      case "task"       => With.configuration.logTaskDuration = ! With.configuration.logTaskDuration
      case "track"      => With.configuration.trackUnit = ! With.configuration.trackUnit

      case "get out"    => quitVsHuman()
      case "quit"       => quitVsHuman()
      case "uninstall"  => quitVsHuman()
      case "surrender"  => quitVsHuman()
      case _            => With.grids.select(text) || With.visualization.tryToggle(text)
    }
    With.game.sendText(text)
  }

  var breakpointFodder = 1
  def breakpoint() {
     breakpointFodder = -breakpointFodder
  }

  def slow(): Unit = {
    With.game.setLocalSpeed(1000)
  }

  def unit: FriendlyUnitInfo = With.units.ours.find(_.selected).get
}

