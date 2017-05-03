package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Information.Battles.BattleTypes.Battle
import Information.Battles.Estimation.BattleEstimationResult
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Pixels.Pixel
import Planning.Yolo
import Utilities.EnrichPixel._
import bwapi.Color

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Armies:")
    With.game.drawTextScreen(521, 18, "+" + formatGain(With.battles.global.estimation.costToEnemy))
    With.game.drawTextScreen(589, 18, "-" + formatGain(With.battles.global.estimation.costToUs))
    With.battles.local.foreach(drawBattle)
    val localBattles = With.battles.local.filter(_.happening)
    if (localBattles.nonEmpty) {
      val battle = localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
      val tactics = battle.bestTactics
      val estimation = battle.estimation(tactics)
      With.game.drawTextScreen(5,   50, With.self.name)
      With.game.drawTextScreen(130, 50, With.enemies.head.name)
      estimation.foreach(drawEstimationReport)
      drawTacticsReport(battle)
    }
    if (Yolo.enabled && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(5, 5, "YOLO")
    }
  }
  
  def formatGain(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor      = With.self.colorDark
    val enemyColor    = With.enemies.head.colorDark
    val neutralColor  = Color.Black
    DrawMap.circle(battle.focus,          8, neutralColor)
    DrawMap.circle(battle.us.vanguard,    8, ourColor)
    DrawMap.circle(battle.enemy.vanguard, 8, enemyColor)
    DrawMap.line(battle.focus, battle.us.vanguard,    ourColor)
    DrawMap.line(battle.focus, battle.enemy.vanguard, enemyColor)
    val topLeft       = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomRight   = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.estimation.costToEnemy >=  battle.estimation.costToUs) ourColor else enemyColor
    DrawMap.box(
      topLeft,
      bottomRight,
      neutralColor)
    DrawMap.labelBox(
      Vector((battle.estimation.netCost).toInt.toString),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawEstimationReport(estimation:BattleEstimationResult) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(255, 50, "+" + 10 * estimation.costToEnemy.toInt)
    With.game.drawTextScreen(255, 75, "-" + 10 * estimation.costToUs.toInt)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  private def getMove(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Movement.Charge))  return "Charge"
    if (tactics.has(Tactics.Movement.Flee))    return "Flee"
    return "-"
  }
  
  private def getFocus(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Focus.Air))    return "Air"
    if (tactics.has(Tactics.Focus.Ground)) return "Ground"
    return "-"
  }
  
  private def getWounded(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Wounded.Flee))  return "Flee"
    return "-"
  }
  
  private def getWorkers(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Workers.FightAll))   return "Fight (All)"
    if (tactics.has(Tactics.Workers.FightHalf))  return "Fight (Half)"
    if (tactics.has(Tactics.Workers.Flee))       return "Flee"
    return "-"
  }
  
  private def drawTacticsReport(battle:Battle) {
  
    drawTacticsReport(battle.bestTactics,           Pixel(5, 76))
    drawTacticsReport(battle.enemy.tacticsApparent, Pixel(155, 76))
    
    if (With.configuration.visualizeBattleTacticsRanks) {
      With.game.drawTextScreen(300, 50, battle.rankedTactics.map(tactic => tactic.toString).mkString("\n"))
    }
  }
  
  private def drawTacticsReport(tactics: TacticsOptions, origin:Pixel) {
    DrawScreen.table(
      origin.x,
      origin.y,
      Vector(
        Vector("Move:",     getMove(tactics)),
        Vector("Focus:",    getFocus(tactics)),
        Vector("Workers:",  getWorkers(tactics)),
        Vector("Wounded:",  getWounded(tactics))
      ))
  }
}
