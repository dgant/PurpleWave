package Planning.Plans.Army

import Planning.Plans.Compound.If
import Planning.Plans.Predicates.SafeToAttack

class ConsiderAttacking extends If(new SafeToAttack) {
  val attack: Attack = new Attack
  whenTrue.set(attack)
}