package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

class BattleLocal(unitsUs: Vector[UnitInfo], unitsEnemy: Vector[UnitInfo]) extends Battle(unitsUs, unitsEnemy) with PredictionLocal{

  val judgmentModifiers: Seq[JudgmentModifier] = JudgmentModifiers(this)
  val gainedValueMultiplier: Double = judgmentModifiers.map(_.gainedValueMultiplier).product
  var judgement: Option[BattleJudgment] = None

  def units: Seq[UnitInfo] = us.units.view ++ enemy.units

  def focus: Pixel = Maff.centroid(teams.map(_.vanguard()))
}
