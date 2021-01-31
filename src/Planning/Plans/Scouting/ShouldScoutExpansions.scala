package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Predicate
import Utilities.GameTime

class ShouldScoutExpansions extends Predicate {
  def time: GameTime = {
    if (With.self.isProtoss)
      if (With.enemy.isTerran)        GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(10, 0)
      else                            GameTime(7, 0)
    else if (With.self.isTerran)
      if (With.enemy.isTerran)        GameTime(8, 0)
      else if (With.enemy.isProtoss)  GameTime(7, 0)
      else                            GameTime(7, 0)
    else
      if (With.enemy.isTerran)        GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(9, 0)
      else                            GameTime(12, 0)
  }

  override def isComplete: Boolean = With.blackboard.wantToAttack() && With.geography.ourBases.size > 1 && With.frame > time()
}
