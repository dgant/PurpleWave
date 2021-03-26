package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) {
  lazy val predictionAttack = new PredictionLocal(this, weAttack = true, weSnipe = false)
  lazy val predictions      = Vector(predictionAttack).distinct

  val judgmentModifiers: Seq[JudgmentModifier] = JudgmentModifiers(this)
  val gainedValueMultiplier: Double = judgmentModifiers.map(_.gainedValueMultiplier).sum
  var judgement: Option[BattleJudgment] = None

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  def focus: Pixel = PurpleMath.centroid(teams.map(_.vanguard()))
}
