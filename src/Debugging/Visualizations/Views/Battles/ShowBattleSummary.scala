package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Battles.Prediction.PredictionLocal
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import Utilities.ByOption
import bwapi.Text

object ShowBattleSummary extends View {
  
  private val tableHeader0  = Pixel(220, 320)
  private val tableHeader1  = tableHeader0.add(125, 0)
  private val tableStart0   = tableHeader0.add(0, 25)
  private val tableStart1   = tableHeader1.add(0, 25)
  private val yolo          = Pixel(310, 230)
  private val tacticsRanks  = Pixel(235, 18)
  
  override def renderScreen() {
    localBattle.foreach(battle => drawEstimationReport(battle.predictionAttack))
    if (With.yolo.active() && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(yolo.bwapi, "YOLO")
    }
  }
  
  override def renderMap() {
    localBattle.foreach(battle => drawBattleMap(battle, battle.predictionAttack))
  }
  
  def localBattle: Option[BattleLocal] = {
    val selectedUnits = With.units.ours.filter(_.selected)
    val localBattles = if (selectedUnits.nonEmpty) selectedUnits.flatMap(_.battle) else With.battles.local
    if (localBattles.isEmpty)
      None
    else
      Some(localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center)))
  }
  
  private def drawBattleMap(battle: BattleLocal, estimation: PredictionLocal) {
    val weWin               = battle.judgement.get.shouldFight
    val ourColorDark        = With.self.colorDark
    val enemyColorDark      = With.enemy.colorDark
    val ourColorNeon        = With.self.colorNeon
    val enemyColorNeon      = With.enemy.colorNeon
    val neutralColor        = Colors.BrightGray
    val winnerStrengthColor = if (weWin) ourColorDark else enemyColorDark
    
    battle.teams.foreach(team => {
      val isUs = team == battle.us
      val centroid = team.centroidAir
      val radius = ByOption.max(team.units.map(u => u.unitClass.radialHypotenuse + u.pixelDistanceCenter(centroid))).getOrElse(0.0).toInt
      val thickness = if (weWin == isUs) 2 else 5
      (0 until thickness).foreach(t => DrawMap.circle(centroid, radius + t, if (isUs) ourColorNeon else enemyColorNeon))
    })
    DrawMap.circle  (battle.focus,          8,                      neutralColor)
    DrawMap.circle  (battle.us.vanguard,    8,                      ourColorDark)
    DrawMap.circle  (battle.enemy.vanguard, 8,                      enemyColorDark)
    DrawMap.line    (battle.focus,          battle.us.vanguard,     ourColorDark)
    DrawMap.line    (battle.focus,          battle.enemy.vanguard,  enemyColorDark)
    With.game.drawCircleMap(battle.focus.bwapi, (battle.us.units ++ battle.enemy.units).map(_.pixelDistanceCenter(battle.focus)).max.toInt, neutralColor)
    DrawMap.labelBox(
      Vector(estimation.localBattleMetrics.lastOption.map(_.totalScore * 100).getOrElse(0.0).toInt.toString),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawEstimationReport(estimation: PredictionLocal) {
    With.game.setTextSize(Text.Size.Large)
    With.game.drawTextScreen(tableHeader0.bwapi, With.self.name)
    With.game.drawTextScreen(tableHeader1.bwapi, With.enemy.name)
    With.game.drawTextScreen(tableStart0.bwapi, "+" + estimation.costToEnemy.toInt)
    With.game.drawTextScreen(tableStart1.bwapi, "-" + estimation.costToUs.toInt)
    With.game.setTextSize(Text.Size.Small)
  }
}

