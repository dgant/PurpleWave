package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Lifecycle.With

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {

  lazy val predictionAttack = new PredictionLocal(this, weAttack = true, weSnipe = false)
  lazy val predictionSnipe  = if (With.self.isZerg) new PredictionLocal(this, weAttack = true, weSnipe = true) else predictionAttack
  lazy val predictions      = Vector(predictionAttack, predictionSnipe).distinct

  var judgement: Option[BattleJudgment] = None
}
