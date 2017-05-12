package Debugging

import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "q"    => breakpoint()
      case "c"    => With.configuration.camera                      = ! With.configuration.camera
      case "v"    => With.configuration.visualize                   = ! With.configuration.visualize
        
      case "bs"   => With.configuration.visualizeBases              = ! With.configuration.visualizeBases
      case "bt"   => With.configuration.visualizeBattles            = ! With.configuration.visualizeBattles
      case "bu"   => With.configuration.visualizeBullets            = ! With.configuration.visualizeBullets
      case "ch"   => With.configuration.visualizeChokepoints        = ! With.configuration.visualizeChokepoints
      case "e"    => With.configuration.visualizeEconomy            = ! With.configuration.visualizeEconomy
      case "g"    => With.configuration.visualizeGeography          = ! With.configuration.visualizeGeography
      case "hm"   => With.configuration.visualizeHeuristicMovement  = ! With.configuration.visualizeHeuristicMovement
      case "hp"   => With.configuration.visualizeHitPoints          = ! With.configuration.visualizeHitPoints
      case "hv"   => With.configuration.visualizeHappyVision        = ! With.configuration.visualizeHappyVision
      case "uf"   => With.configuration.visualizeUnitsForeign       = ! With.configuration.visualizeUnitsForeign
      case "uo"   => With.configuration.visualizeUnitsOurs          = ! With.configuration.visualizeUnitsOurs
      case "p"    => With.configuration.visualizePerformance        = ! With.configuration.visualizePerformance
      case "pd"   => With.configuration.visualizePerformanceDetails = ! With.configuration.visualizePerformanceDetails
      case "pl"   => With.configuration.visualizePlans              = ! With.configuration.visualizePlans
      case "re"   => With.configuration.visualizeRealEstate          = ! With.configuration.visualizeRealEstate
      case "r"    => With.configuration.visualizeResources          = ! With.configuration.visualizeResources
      case "s"    => With.configuration.visualizeScheduler          = ! With.configuration.visualizeScheduler
      case "1"    => With.game.setLocalSpeed(1000)   ; With.configuration.camera = false
      case "2"    => With.game.setLocalSpeed(60)     ; With.configuration.camera = false
      case "3"    => With.game.setLocalSpeed(30)     ; With.configuration.camera = false
      case "4"    => With.game.setLocalSpeed(0)      ; With.configuration.camera = false
    }
  }
  
  def breakpoint() {
    val setABreakpointHere = 12345
  }
}
