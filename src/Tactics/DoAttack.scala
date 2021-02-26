
package Tactics

import Lifecycle.With
import Micro.Squads.Goals.GoalAttack

class DoAttack extends Squadify[GoalAttack] {

  override val goal: GoalAttack = new GoalAttack

  override def update() {
    if (With.blackboard.wantToAttack()) {
      super.update()
    }
  }
}
