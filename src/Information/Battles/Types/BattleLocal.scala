package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import Utilities.ByOption

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  lazy val predictionAttack = new PredictionLocal(this, weAttack = true, weSnipe = false)
  lazy val predictions      = Vector(predictionAttack).distinct

  var judgement: Option[NewBattleJudgment] = None

  def focus: Pixel = PurpleMath.centroid(teams.map(_.vanguard))

  def updateFoci(): Unit = {
    teams.foreach(team => {
      val hasGround         = team.units.exists( ! _.flying)
      val centroidAir       = PurpleMath.centroid(team.units.view.map(_.pixelCenter))
      val centroidGround    = if (hasGround) PurpleMath.centroid(team.units.view.filterNot(_.flying).map(_.pixelCenter)) else team.centroidAir.nearestWalkableTile.pixelCenter
      team.centroidAir      = PurpleMath.centroid(team.units.view.map(_.pixelCenter))
      // Should probably switch to ground distance, but for performance
      team.centroidGround   = ByOption.minBy(team.units.view.filterNot(_.flying && hasGround))(_.pixelDistanceSquared(centroidGround)).map(_.pixelCenter).getOrElse(centroidGround)

    })

    teams.foreach(group =>
      group.vanguard = ByOption
        .minBy(group.units)(_.pixelDistanceSquared(group.opponent.centroidAir))
        .map(_.pixelCenter)
        .getOrElse(SpecificPoints.middle))
  }
}
