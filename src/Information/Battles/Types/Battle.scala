package Information.Battles.Types

import Information.Battles.Estimation.Estimation
import Lifecycle.With
import Mathematics.Points.Pixel
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  var estimationGeometric = new Estimation
  var estimationAbstract  = new Estimation
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  
  def focus: Pixel = teams.map(_.vanguard).centroid
  
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttackThisSecond))
  
  def uphillEnemy: Boolean = With.grids.altitudeBonus.get(us.centroid.tileIncluding) < With.grids.altitudeBonus.get(enemy.centroid.tileIncluding)
  def acrossChoke: Boolean = us.centroid.zone != enemy.centroid.zone
}
