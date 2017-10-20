package Planning

import Lifecycle.With

class Blackboard {
  
  var maxFramesToSendAdvanceBuilder: Int = With.configuration.maxFramesToSendAdvanceBuilder
  
  var aggressionRatio   : Double  = 1.0
  var gasBankSoftLimit  : Int     = 450
  var gasBankHardLimit  : Int     = Int.MaxValue
  var allIn             : Boolean = false
}
