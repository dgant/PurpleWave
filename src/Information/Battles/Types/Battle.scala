package Information.Battles.Types

import Information.Battles.Estimation.Estimator
import Mathematics.Points.Pixel
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle       = this
  enemy.battle    = this
  
  var estimationGeometric:  Estimator = new Estimator(this, considerGeometry = true)
  var estimationAbstract:   Estimator = new Estimator(this, considerGeometry = false)
  
  //////////////
  // Features //
  //////////////
  
  def groups: Vector[Team] = Vector(us, enemy)
  
  def focus: Pixel = groups.map(_.vanguard).centroid
  
  def happening: Boolean = groups.forall(_.units.nonEmpty) && groups.exists(_.units.exists(_.canAttackThisSecond))
}
