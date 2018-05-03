package Planning.Plans.Predicates

import Lifecycle.With

class SafeAtHome extends SafeToAttack {
  override def isComplete: Boolean = super.isComplete || With.battles.global.globalSafeToDefend
}