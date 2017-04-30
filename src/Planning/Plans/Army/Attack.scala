package Planning.Plans.Army

import Planning.Composition.PixelFinders.Tactics.TileEnemyBase
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Information.{FindEnemyBase, FoundEnemyBase}
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Attack extends IfThenElse {
  
  val attack = this
  description.set("Attack")
  
  val attackers = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchWarriors);
    unitCounter.set(UnitCountEverything)
  })
  
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase { scouts.inherit(attackers) })
  whenTrue.set(new ControlPixel {
    units.inherit(attackers);
    positionToControl.set(new TileEnemyBase);
    description.inherit(attack.description)
  })
}
