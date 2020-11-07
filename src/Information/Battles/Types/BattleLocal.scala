package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Terran

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {

  private def canSnipe = (
    // Acceptable, performance-wise
    (With.reaction.clusteringAverage < 8 || With.frame < GameTime(12, 0)())
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
}
