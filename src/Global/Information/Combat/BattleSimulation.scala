package Global.Information.Combat

import bwapi.Position


class BattleSimulation(
  val ourGroup:BattleGroup,
  val enemyGroup:BattleGroup) {
  
  val focalPoint = new Position(
    (ourGroup.vanguard.getX + enemyGroup.vanguard.getX) / 2,
    (ourGroup.vanguard.getY + enemyGroup.vanguard.getY) / 2)
  
  var ourScore = 0
  var enemyScore = 0
  
  def success = ourScore >= enemyScore
}
