package Information.Battles.Types

import Information.Battles.Estimation.BattleEstimation
import Mathematics.Pixels.Pixel

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  us.battle       = this
  enemy.battle    = this
  us.opponent     = enemy
  enemy.opponent  = us
  
  var estimation:BattleEstimation = new BattleEstimation(Some(this), true)
  
  //////////////
  // Features //
  //////////////
  
  def groups: Iterable[BattleGroup] = Vector(us, enemy)
  
  def happening: Boolean = us.units.nonEmpty && enemy.units.nonEmpty && (us.units.exists(_.canAttackThisSecond) || enemy.units.exists(_.canAttackThisSecond))
  
  def focus: Pixel = us.vanguard.midpoint(enemy.vanguard)
}
