package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Battles.Prediction.Prediction
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import Planning.Yolo
import Utilities.ByOption

object ShowBattleSummary extends View {
  
  private val tableHeader0  = Pixel(220, 320)
  private val tableHeader1  = tableHeader0.add(125, 0)
  private val tableStart0   = tableHeader0.add(0, 25)
  private val tableStart1   = tableHeader1.add(0, 25)
  private val army0         = Pixel(438, 18)
  private val army1         = Pixel(521, 18)
  private val army2         = Pixel(589, 18)
  private val yolo          = Pixel(310, 230)
  private val tacticsRanks  = Pixel(235, 18)
  
  override def renderScreen() {
    With.game.drawTextScreen(army0.bwapi, "Offense:")
    With.game.drawTextScreen(army1.bwapi, "+" + With.battles.global.estimationAbstractOffense.costToEnemy.toInt + " x " + "%1.2f".format(With.blackboard.aggressionRatio.get))
    With.game.drawTextScreen(army2.bwapi, "-" + With.battles.global.estimationAbstractOffense.costToUs.toInt)
    localBattle.foreach(battle => drawEstimationReport(battle.estimationSimulationAttack))
    if (Yolo.active && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(yolo.bwapi, "YOLO")
    }
  }
  
  override def renderMap() {
    localBattle.foreach(battle => drawBattleMap(battle, battle.estimationSimulationAttack))
  }
  
  def localBattle: Option[BattleLocal] = {
    val selectedUnits = With.units.ours.filter(_.selected)
    val localBattles = if (selectedUnits.nonEmpty) selectedUnits.flatMap(_.battle) else With.battles.local
    if (localBattles.isEmpty)
      None
    else
      Some(localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center)))
  }
  
  private def drawBattleMap(battle: BattleLocal, estimation: Prediction) {
    val weWin               = battle.shouldFight
    val ourColorDark        = With.self.colorDark
    val enemyColorDark      = With.enemy.colorDark
    val ourColorNeon        = With.self.colorNeon
    val enemyColorNeon      = With.enemy.colorNeon
    val neutralColor        = Colors.BrightGray
    val winnerStrengthColor = if (weWin) ourColorDark else enemyColorDark
    
    battle.teams.foreach(team => {
      val isUs = team == battle.us
      val centroid = team.centroid
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
      Vector(estimation.netValue.toInt.toString),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawEstimationReport(estimation: Prediction) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(tableHeader0.bwapi, With.self.name)
    With.game.drawTextScreen(tableHeader1.bwapi, With.enemy.name)
    With.game.drawTextScreen(tableStart0.bwapi, "+" + estimation.costToEnemy.toInt)
    With.game.drawTextScreen(tableStart1.bwapi, "-" + estimation.costToUs.toInt)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}

