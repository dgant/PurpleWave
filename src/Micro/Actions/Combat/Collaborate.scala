package Micro.Actions.Combat

import Information.Battles.TacticsTypes.Tactics
import Micro.Actions.Action
import Micro.Intent.Intention
import Planning.Yolo

object Collaborate extends Action {
  
  override def perform(intent: Intention) {
    if ( ! Yolo.active) {
      if (retreatArmy(intent)) {
        Kite.consider(intent)
        Flee.consider(intent)
      }
      if (retreatWounded(intent)) {
        Flee.consider(intent)
      }
      if (retreatWorker(intent)) {
        Flee.consider(intent)
      }
    }
    
    Charge.consider(intent)
  }
  
  private def retreatWorker   (intent:Intention):Boolean = workersFlee  (intent)  &&   isWorker(intent)
  private def retreatWounded  (intent:Intention):Boolean = woundedFlee  (intent)  &&   isWounded(intent)
  private def retreatArmy     (intent:Intention):Boolean = fightersFlee (intent)  && ! isWorker(intent)
  
  private def fightersFlee  (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Movement.Flee))
  private def woundedFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Wounded.Flee))
  private def workersFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Workers.Flee))
  private def isWounded     (intent:Intention)  : Boolean = intent.unit.wounded
  private def isWorker      (intent:Intention)  : Boolean = intent.unit.unitClass.isWorker
}
