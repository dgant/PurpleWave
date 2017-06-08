package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.PixelFinders.Tactics.TileEnemyBase
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plans.Compound.{If, IfThenElse}
import Planning.Plans.Information.{FindEnemyBase, FoundEnemyBase}

class ConsiderAttacking
  extends IfThenElse(
    new If(() => With.battles.global.estimation.weWin)) {
  
  val attackers = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchWarriors)
    unitCounter.set(UnitCountEverything)
  })
  
  val control = new ControlPixel
  control.controllers.inherit(attackers)
  control.positionToControl.set(new TileEnemyBase)
    
  val scout = new FindEnemyBase
  scout.scouts.inherit(attackers)
  
  val attackOrScout = new IfThenElse(new FoundEnemyBase, control, scout)
  whenTrue.set(attackOrScout)
}
