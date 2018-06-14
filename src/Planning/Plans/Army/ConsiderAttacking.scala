package Planning.Plans.Army

import Planning.Plans.Compound.If
import Planning.Predicates.Reactive.SafeToMoveOut

class ConsiderAttacking extends If(new SafeToMoveOut) {
  val attack: Attack = new Attack
  whenTrue.set(attack)
}