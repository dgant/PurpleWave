package Planning.Plans.Army

import Lifecycle.With
import Planning.Plans.Compound.{Check, If}
import Planning.Yolo

class ConsiderAttacking extends If(
  new Check(() => Yolo.active || With.battles.global.estimationAbstractOffense.netValue >= 0)) {
      
  val attack: Attack = new Attack
  whenTrue.set(attack)
}