package Information.Battles

import bwapi.Position
import Utilities.EnrichPosition._

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  def focus     : Position = us.vanguard.midpoint(enemy.vanguard)
  def groups    : Iterable[BattleGroup] = List(us, enemy)
  def happening : Boolean =
    us.units.nonEmpty &&
    enemy.units.nonEmpty &&
    (us.units.exists(_.canAttackThisSecond) ||
    enemy.units.exists(_.canAttackThisSecond))
  
  var ourLosses   : Int = 0
  var enemyLosses : Int = 0
}
