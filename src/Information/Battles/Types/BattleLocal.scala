package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class BattleLocal(us: Team, enemy: Team) extends Battle(us, enemy) with PredictionLocal{

  val judgmentModifiers: Seq[JudgmentModifier] = JudgmentModifiers(this)
  val gainedValueMultiplier: Double = judgmentModifiers.map(_.gainedValueMultiplier).sum
  var judgement: Option[BattleJudgment] = None

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  def focus: Pixel = PurpleMath.centroid(teams.map(_.vanguard()))
}
