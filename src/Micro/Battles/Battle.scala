package Micro.Battles

import bwapi.Position
import Utilities.EnrichPosition._

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  def focus:Position = us.vanguard.midpoint(enemy.vanguard)
}
