package Information.Battles.Types

import Information.Battles.Prediction.PredictionLocal
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

class BattleLocal(unitsUs: Seq[UnitInfo], unitsEnemy: Seq[UnitInfo]) extends Battle(unitsUs, unitsEnemy) with PredictionLocal {
  val judgmentModifiers: Seq[JudgmentModifier] = JudgmentModifiers(this)
  var judgement: Option[BattleJudgment] = None
  override val speedMultiplier: Double = judgmentModifiers.map(_.speedMultiplier).product
  def units: Seq[UnitInfo] = us.units.view ++ enemy.units
  def focus: Pixel = Maff.centroid(teams.map(_.vanguardAll()))

}
