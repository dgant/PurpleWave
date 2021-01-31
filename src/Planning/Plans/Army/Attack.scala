
package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan

class Attack extends Plan {
  override def onUpdate() {
    With.blackboard.wantToAttack.set(true)
  }
}
