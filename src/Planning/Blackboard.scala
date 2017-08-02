package Planning

import Lifecycle.With

class Blackboard {
  
  var maxFramesToSendAdvanceBuilder: Int = With.configuration.maxFramesToSendAdvanceBuilder

  var battleDesire = 1.0
  var allIn: Boolean = false
  var aggressionRatio: Double = 1.0
  
  var zergWasTryingToExpand = false
}
