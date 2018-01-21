package Planning

import Lifecycle.With

class Blackboard {
  
  var maxFramesToSendAdvanceBuilder: Int = With.configuration.maxFramesToSendAdvanceBuilder
  
  var aggressionRatio   : Double  = 1.0
  var safetyRatio       : Double  = 1.2
  var gasLimitFloor     : Int     = 450
  var gasLimitCeiling   : Int     = 100000
  var gasTargetRatio    : Double  = if (With.self.isProtoss) 3.0 / 10.0 else 3.0 / 8.0
  var allIn             : Boolean = false
}
