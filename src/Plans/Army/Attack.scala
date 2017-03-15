package Plans.Army

import Plans.Allocation.LockUnits
import Plans.Compound.IfThenElse
import Plans.Information.{FindEnemyBase, FoundEnemyBase}
import Strategies.PositionFinders.PositionEnemyBase
import Strategies.UnitCountEverything
import Strategies.UnitMatchers.UnitMatchWarriors
import Utilities.Property

class Attack extends IfThenElse {
  val attackers = new Property[LockUnits](new LockUnits { unitMatcher.set(UnitMatchWarriors); unitCounter.set(UnitCountEverything)})
  
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase { scouts.inherit(attackers) })
  whenTrue.set(new ControlPosition { units.inherit(attackers); position.set(new PositionEnemyBase) })
}
