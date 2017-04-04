package Debugging

import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "c" => With.configuration.enableCamera = ! With.configuration.enableCamera
      case "v" => With.configuration.enableVisualization = ! With.configuration.enableVisualization
      case "1" => With.game.setLocalSpeed(10000)  ; With.configuration.enableCamera = false
      case "2" => With.game.setLocalSpeed(60)     ; With.configuration.enableCamera = false
      case "3" => With.game.setLocalSpeed(30)     ; With.configuration.enableCamera = false
      case "4" => With.game.setLocalSpeed(0)      ; With.configuration.enableCamera = false
    }
  }
}
