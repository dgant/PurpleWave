package Planning.Predicates.Reactive

import Lifecycle.With

class SafeAtHome extends SafeToMoveOut {
  override def isComplete: Boolean = {
    With.battles.global.globalSafeToDefend || super.isComplete
  }
}