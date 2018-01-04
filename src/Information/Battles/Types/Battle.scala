package Information.Battles.Types

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

abstract class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
  def focus: Pixel = teams.map(_.vanguard).centroid
  
  lazy val analysis = new BattleAnalysis(this)
  lazy val desire: Double = analysis.desireTotal
}
