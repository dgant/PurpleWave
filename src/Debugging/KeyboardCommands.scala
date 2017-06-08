package Debugging

import Debugging.Visualizations.Views.Battles.ViewBattles
import Debugging.Visualizations.Views.Economy.ViewEconomy
import Debugging.Visualizations.Views.Fun.{ViewHappy, ViewTextOnly}
import Debugging.Visualizations.Views.Geography.ViewGeography
import Debugging.Visualizations.Views.Micro.ViewMicro
import Debugging.Visualizations.Views.Performance.ViewPerformance
import Debugging.Visualizations.Views.Planning.ViewPlanning
import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "q"    => breakpoint()
      case "c"    => With.configuration.camera      = ! With.configuration.camera
      case "v"    => With.configuration.visualize   = ! With.configuration.visualize
      case "cy"   => With.configuration.cycleViews  = ! With.configuration.cycleViews
      case "b "   => With.visualization.setView(ViewBattles)
      case "e"    => With.visualization.setView(ViewEconomy)
      case "g"    => With.visualization.setView(ViewGeography)
      case "m"    => With.visualization.setView(ViewMicro)
      case "hv"   => With.visualization.setView(ViewHappy)
      case "to"   => With.visualization.setView(ViewTextOnly)
      case "p"    => With.visualization.setView(ViewPerformance)
      case "pl"   => With.visualization.setView(ViewPlanning)
      case "1"    => With.game.setLocalSpeed(1000)  ; With.configuration.camera = false
      case "2"    => With.game.setLocalSpeed(60)    ; With.configuration.camera = false
      case "3"    => With.game.setLocalSpeed(30)    ; With.configuration.camera = false
      case "4"    => With.game.setLocalSpeed(0)     ; With.configuration.camera = false
      case "map"  => With.logger.debug("The current map is " + With.game.mapName + ": " + With.game.mapFileName)
    }
  }
  
  def breakpoint() {
    val setABreakpointHere = 12345
  }
}
