package Information.Battles

import bwapi.Position
import Utilities.EnrichPosition._

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  def focus:Position = us.vanguard.midpoint(enemy.vanguard)
  def groups:Iterable[BattleGroup] = List(us, enemy)
  def happening:Boolean = us.units.nonEmpty && enemy.units.nonEmpty
}
