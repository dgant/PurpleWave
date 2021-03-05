package Planning.Predicates.Reactive

import Lifecycle.With

class SafeAtHome extends SafeToMoveOut {
  override def apply: Boolean = With.battles.global.globalSafeToDefend || super.apply
}