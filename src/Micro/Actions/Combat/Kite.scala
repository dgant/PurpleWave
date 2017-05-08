package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Intent.Intention

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = (
    intent.unit.canMoveThisFrame
    && intent.targets.nonEmpty
    && intent.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(intent: Intention) {
    
    if (intent.unit.cooldownLeft > 0) {
      Reposition.delegate(intent)
    }
    if (intent.threatsActive.forall(_.topSpeed < intent.unit.topSpeed) && intent.threatsActive.exists(_.pixelDistanceFast())
    
    //Else shoot?
  }
}
