package Debugging

import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "c" => With.configuration.camera = ! With.configuration.camera
      case "v" => With.configuration.visualize = ! With.configuration.visualize
      case "1" => With.game.setLocalSpeed(10000)  ; With.configuration.camera = false
      case "2" => With.game.setLocalSpeed(60)     ; With.configuration.camera = false
      case "3" => With.game.setLocalSpeed(30)     ; With.configuration.camera = false
      case "4" => With.game.setLocalSpeed(0)      ; With.configuration.camera = false
    }
  }
}
