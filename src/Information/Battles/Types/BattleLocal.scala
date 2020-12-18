package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Terran
import Utilities.{ByOption, Minutes}

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {

  private def canSnipe = false && (
    // Acceptable, performance-wise
    (With.reaction.clusteringAverage < 8 || With.frame < Minutes(12)())
    // Trying to snipe tanks leads to nasty surprises; tanks in fog tend to blast us and dissuade the snipe
    && (
      ! With.enemies.exists(_.hasTech(Terran.SiegeMode))
      || ! enemy.units.exists(_.is(UnitMatchSiegeTank))
      || us.units.view.map(u => u.subjectiveValue * (if (u.flying) 1 else -1)).sum > 0
    )
    // Snipe simulation is just really bad against long range static defense
    && (
      ! enemy.units.exists(u => u.unitClass.isStaticDefense && u.matchups.targets.nonEmpty)
    ))

  lazy val predictionAttack = new PredictionLocal(this, weAttack = true, weSnipe = false)
  lazy val predictionSnipe  = if (canSnipe) new PredictionLocal(this, weAttack = true, weSnipe = true) else predictionAttack
  lazy val predictions      = Vector(predictionAttack, predictionSnipe).distinct

  var judgement: Option[BattleJudgment] = None

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
