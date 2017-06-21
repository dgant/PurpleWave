package Information.Battles.Types

import Information.Battles.Estimation.BattleEstimation
import Mathematics.Points.Pixel
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : BattleGroup,
  val enemy : BattleGroup) {
  
  us.battle       = this
  enemy.battle    = this
  
  var estimationGeometric:  BattleEstimation = new BattleEstimation(Some(this), considerGeometry = true)
  var estimationAbstract:   BattleEstimation = new BattleEstimation(Some(this), considerGeometry = false)
  
  //////////////
  // Features //
  //////////////
  
  def groups: Vector[BattleGroup] = Vector(us, enemy)
  
  def focus: Pixel = groups.map(_.vanguard).centroid
  
  def happening: Boolean = groups.forall(_.units.nonEmpty) && groups.exists(_.units.exists(_.canAttackThisSecond))
  
  
}
