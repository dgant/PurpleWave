package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Battles.Estimation.EstimationBuilder
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.Pixel
import Planning.Yolo
import Utilities.EnrichPixel._

object VisualizeBattles {
  
  private val graphMargin             = Pixel(2, 2)
  private val graphWidth              = 90
  private val graphHeight             = 90 + With.visualization.lineHeightSmall
  private val tableHeader0            = Pixel(220, 320)
  private val tableHeader1            = tableHeader0.add(125, 0)
  private val tableStart0             = tableHeader0.add(0, 25)
  private val tableStart1             = tableHeader1.add(0, 25)
  private val army0                   = Pixel(438, 18)
  private val army1                   = Pixel(521, 18)
  private val army2                   = Pixel(589, 18)
  private val yolo                    = graphMargin
  private val tacticsRanks            = Pixel(235, 18)
  
  def render() {
    With.game.drawTextScreen(army0.bwapi, "Overall:")
    With.game.drawTextScreen(army1.bwapi, "+" + With.battles.global.estimationGeometric.result.costToEnemy.toInt)
    With.game.drawTextScreen(army2.bwapi, "-" + With.battles.global.estimationGeometric.result.costToUs.toInt)
    With.battles.local.foreach(drawBattle)
    val localBattles = With.battles.local
    if (localBattles.nonEmpty) {
      val battle      = localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
      val estimation  = battle.estimationGeometric
      drawEstimationReport(estimation)
    }
    if (Yolo.active && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(yolo.bwapi, "YOLO")
    }
  }
  
  private def drawBattle(battle:Battle) {
    val ourColor            = With.self.colorDark
    val enemyColor          = With.enemies.head.colorDark
    val neutralColor        = Colors.NeonOrange
    val topLeft             = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomRight         = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.estimationGeometric.result.costToEnemy >=  battle.estimationGeometric.result.costToUs) ourColor else enemyColor
    DrawMap.circle  (battle.focus,          8,                      neutralColor)
    DrawMap.circle  (battle.us.vanguard,    8,                      ourColor)
    DrawMap.circle  (battle.enemy.vanguard, 8,                      enemyColor)
    DrawMap.line    (battle.focus,          battle.us.vanguard,     ourColor)
    DrawMap.line    (battle.focus,          battle.enemy.vanguard,  enemyColor)
    DrawMap.box     (topLeft,               bottomRight,            neutralColor)
    DrawMap.labelBox(
      Vector(battle.estimationGeometric.result.netCost.toInt.toString),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawEstimationReport(estimation:EstimationBuilder) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(tableHeader0.bwapi, With.self.name)
    With.game.drawTextScreen(tableHeader1.bwapi, With.enemies.head.name)
    With.game.drawTextScreen(tableStart0.bwapi, "+" + estimation.result.costToEnemy.toInt)
    With.game.drawTextScreen(tableStart1.bwapi, "-" + estimation.result.costToUs.toInt)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    
    
  }
}

