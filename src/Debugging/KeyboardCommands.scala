package Debugging

import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "q"  => breakpoint()
      case "c"  => With.configuration.camera                      = ! With.configuration.camera
      case "v"  => With.configuration.visualize                   = ! With.configuration.visualize
      case "e"  => With.configuration.visualizeEconomy            = ! With.configuration.visualizeEconomy
      case "p"  => With.configuration.visualizePerformance        = ! With.configuration.visualizePerformance
      case "pd" => With.configuration.visualizePerformanceDetails = ! With.configuration.visualizePerformanceDetails
      case "r"  => With.configuration.visualizeResources          = ! With.configuration.visualizeResources
      case "s"  => With.configuration.visualizeScheduler          = ! With.configuration.visualizeScheduler
      case "pl" => With.configuration.visualizePlans              = ! With.configuration.visualizePlans
      case "1"  => With.game.setLocalSpeed(10000)  ; With.configuration.camera = false
      case "2"  => With.game.setLocalSpeed(60)     ; With.configuration.camera = false
      case "3"  => With.game.setLocalSpeed(30)     ; With.configuration.camera = false
      case "4"  => With.game.setLocalSpeed(0)      ; With.configuration.camera = false
    }
  }
  
  def breakpoint() {
    val setABreakpointHere = 12345
  }
}
