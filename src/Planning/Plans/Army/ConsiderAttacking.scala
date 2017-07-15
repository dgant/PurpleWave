package Planning.Plans.Army

import Lifecycle.With
import Planning.Plans.Compound.{Check, If}
import Planning.Yolo

class ConsiderAttacking extends If(
    new Check(() =>
      Yolo.active
      ||    With.battles.global.estimationAbstractOffense.weGainValue
      ||  ! With.battles.global.estimationAbstractOffense.weLoseValue)) {
      
  val attack: Attack = new Attack
  whenTrue.set(attack)
}