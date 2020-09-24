package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

abstract class Battle(val us: Team, val enemy: Team) {
  us.battle = this
  enemy.battle = this
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
  def focus: Pixel = PurpleMath.centroid(teams.map(_.vanguard))

  def updateFoci(): Unit = {
     teams.foreach(group => {
      val airCentroid = PurpleMath.centroid(group.units.view.map(_.pixelCenter))
      val hasGround   = group.units.exists( ! _.flying)
      group.centroid  = ByOption
        .minBy(group.units.view.filterNot(_.flying && hasGround))(_.pixelDistanceSquared(airCentroid))
        .map(_.pixelCenter)
        .getOrElse(airCentroid)
    })

    teams.foreach(group =>
      group.vanguard = ByOption
        .minBy(group.units)(_.pixelDistanceCenter(group.opponent.centroid))
        .map(_.pixelCenter)
        .getOrElse(SpecificPoints.middle))
  }
}
