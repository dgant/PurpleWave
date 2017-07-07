package Planning.Plans.Army

import Lifecycle.With
import Planning.Plans.Compound.{Check, If}
import Planning.Yolo

class ConsiderAttacking extends If(
    new Check(() =>
      Yolo.active
      ||    With.battles.global.estimationAbstract.weGainValue
      ||  ! With.battles.global.estimationAbstract.weLoseValue)) {
      
  val attack: Attack = new Attack
  whenTrue.set(attack)
}