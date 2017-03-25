package Planning.Plans.Army

import Planning.Plans.Allocation.LockUnits
import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Information.{FindEnemyBase, FoundEnemyBase}
import Planning.Composition.PositionFinders.PositionEnemyBase
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Attack extends IfThenElse {
  
  description.set("Attack")
  
  val attackers = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchWarriors);
    unitCounter.set(UnitCountEverything)
  })
  
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase { scouts.inherit(attackers) })
  whenTrue.set(new ControlPosition { units.inherit(attackers); position.set(new PositionEnemyBase) })
}
