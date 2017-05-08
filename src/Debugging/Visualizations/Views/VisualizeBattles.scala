package Debugging.Visualizations.Views

import Debugging.Visualizations.{Colors, Visualization}
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Information.Battles.BattleTypes.Battle
import Information.Battles.Estimation.{BattleEstimationState, BattleEstimationResult}
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Pixels.Pixel
import Planning.Yolo
import ProxyBwapi.Players.PlayerInfo
import Utilities.EnrichPixel._
import bwapi.Color

import scala.collection.mutable.ArrayBuffer

object VisualizeBattles {
  
  private val graphMargin             = Pixel(2, 2)
  private val graphDimensions         = Pixel(90, 90 + Visualization.lineHeightSmall)
  private val healthGraphAreaStart    = Pixel(5,  18)
  private val healthGraphAreaEnd      = healthGraphAreaStart.add(graphDimensions)
  private val healthGraphStart        = healthGraphAreaStart.add(graphMargin).add(0, Visualization.lineHeightSmall)
  private val healthGraphEnd          = healthGraphAreaEnd.subtract(graphMargin)
  private val positionGraphAreaStart  = Pixel(healthGraphStart.x, healthGraphEnd.y + Visualization.lineHeightSmall)
  private val positionGraphAreaEnd    = positionGraphAreaStart.add(graphDimensions)
  private val positionGraphStart      = positionGraphAreaStart.add(graphMargin).add(0, Visualization.lineHeightSmall)
  private val positionGraphEnd        = positionGraphAreaEnd.subtract(graphMargin)
  private val tableHeader0            = Pixel(100, 18)
  private val tableHeader1            = tableHeader0.add(125, 0)
  private val tableStart0             = tableHeader0.add(0, 25)
  private val tableStart1             = tableHeader1.add(0, 25)
  private val army0                   = Pixel(438, 18)
  private val army1                   = Pixel(521, 18)
  private val army2                   = Pixel(589, 18)
  private val yolo                    = Pixel(5, 5)
  private val tacticsRanks            = Pixel(235, 18)
  
  def render() {
    With.game.drawTextScreen(army0.bwapi, "Overall:")
    With.game.drawTextScreen(army1.bwapi, "+" + With.battles.global.estimation.costToEnemy.toInt)
    With.game.drawTextScreen(army2.bwapi, "-" + With.battles.global.estimation.costToUs.toInt)
    With.battles.local.foreach(drawBattle)
    val localBattles = With.battles.local
    if (localBattles.nonEmpty) {
      val battle      = localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
      val tactics     = battle.bestTactics
      val estimation  = battle.estimation(tactics)
      estimation.foreach(drawEstimationReport)
      drawTacticsReport(battle)
    }
    if (Yolo.active && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(yolo.bwapi, "YOLO")
    }
  }
  
  private def drawBattle(battle:Battle) {
    val ourColor            = With.self.colorDark
    val enemyColor          = With.enemies.head.colorDark
    val neutralColor        = Color.Black
    val topLeft             = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomRight         = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.estimation.costToEnemy >=  battle.estimation.costToUs) ourColor else enemyColor
    DrawMap.circle  (battle.focus,          8,                      neutralColor)
    DrawMap.circle  (battle.us.vanguard,    8,                      ourColor)
    DrawMap.circle  (battle.enemy.vanguard, 8,                      enemyColor)
    DrawMap.line    (battle.focus,          battle.us.vanguard,     ourColor)
    DrawMap.line    (battle.focus,          battle.enemy.vanguard,  enemyColor)
    DrawMap.box     (topLeft,               bottomRight,            neutralColor)
    DrawMap.labelBox(
      Vector(battle.estimation.netCost.toInt.toString),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawEstimationReport(estimation:BattleEstimationResult) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(tableHeader0.bwapi, "+" + estimation.costToEnemy.toInt)
    With.game.drawTextScreen(tableHeader1.bwapi, "-" + estimation.costToUs.toInt)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    
    if (estimation.statesUs.size < 2) return
  
    With.game.drawBoxScreen(healthGraphAreaStart.bwapi, healthGraphAreaEnd.bwapi,  Color.Black, true)
    With.game.drawBoxScreen(healthGraphStart.bwapi,     healthGraphEnd.bwapi,      Colors.DarkGray)
    With.game.drawTextScreen(healthGraphStart.subtract(0, Visualization.lineHeightSmall).bwapi, "Health:")
    drawHealthGraph(estimation.statesUs,     With.self)
    drawHealthGraph(estimation.statesEnemy,  With.enemies.head)
    
    val allStates = estimation.statesUs ++ estimation.statesEnemy
    val xMin = allStates.map(_.x).min
    val xMax = allStates.map(_.x).max
  
    With.game.drawBoxScreen(positionGraphAreaStart.bwapi, positionGraphAreaEnd.bwapi,  Color.Black, true)
    With.game.drawBoxScreen(positionGraphStart.bwapi,     positionGraphEnd.bwapi,      Colors.DarkGray)
    With.game.drawTextScreen(positionGraphStart.subtract(0, Visualization.lineHeightSmall).bwapi, "Position:")
    drawPositionGraph(estimation.statesUs,     xMin, xMax, With.self)
    drawPositionGraph(estimation.statesEnemy,  xMin, xMax, With.enemies.head)
  }
  
  def drawHealthGraph(
    states: ArrayBuffer[BattleEstimationState],
    player: PlayerInfo) {
  
    val valueMax = states.head.avatar.totalHealth
    val xScale  = (healthGraphEnd.x - healthGraphStart.x - 2 * graphMargin.x) / (states.size - 1).toDouble
    val yScale  = (healthGraphEnd.y - healthGraphStart.y - 2 * graphMargin.y) / valueMax
  
    var i = 0
    while (i < states.size - 1) {
      val xBefore = graphMargin.x + healthGraphStart.x  + xScale * i
      val xAfter  = graphMargin.x + healthGraphStart.x  + xScale * (i + 1)
      val yBefore = graphMargin.y + healthGraphEnd.y    - (states(i  ).avatar.totalHealth - states(i  ).damageReceived) * yScale
      val yAfter  = graphMargin.y + healthGraphEnd.y    - (states(i+1).avatar.totalHealth - states(i+1).damageReceived) * yScale
      With.game.drawLineScreen(xBefore.toInt, yBefore.toInt, xAfter.toInt, yAfter.toInt, player.colorNeon)
      i += 1
    }
  }
  
  def drawPositionGraph(
    states  : ArrayBuffer[BattleEstimationState],
    xMin    : Double,
    xMax    : Double,
    player  : PlayerInfo) {
  
    
  
    val colorMedium = player.colorMedium
    val colorNeon   = player.colorNeon
    val xScale      = (positionGraphEnd.x - positionGraphStart.x - 2 * graphMargin.x) / (states.size - 1).toDouble
    val yScale      = (positionGraphEnd.y - positionGraphStart.y - 2 * graphMargin.y) / (xMax - xMin)
    
    var i = 0
    while (i < states.size - 1) {
      val xStart    = graphMargin.x + positionGraphStart.x + (xScale *  i     ).toInt
      val xEnd      = graphMargin.x + positionGraphStart.x + (xScale * (i + 1)).toInt
      val yMiddle0  = graphMargin.y + positionGraphStart.y + (yScale * (healthGraphAreaStart.y + states(i  ).x - xMin)).toInt
      val yMiddle1  = graphMargin.y + positionGraphStart.y + (yScale * (healthGraphAreaStart.y + states(i+1).x - xMin)).toInt
      With.game.drawLineScreen(xStart, yMiddle0,            xEnd, yMiddle1,            colorNeon)
      i += 1
    }
  }
  
  private def getMove(tactics:TacticsOptions):String = {
    if      (tactics.has(Tactics.Movement.Charge))  "Charge"
    else if (tactics.has(Tactics.Movement.Flee))    "Flee"
    else                                            "-"
  }
  
  private def getFocus(tactics:TacticsOptions):String = {
    if      (tactics.has(Tactics.Focus.Air))    "Air"
    else if (tactics.has(Tactics.Focus.Ground)) "Ground"
    else                                        "-"
  }
  
  private def getWounded(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Wounded.Flee))  "Flee"
    else                                    "-"
  }
  
  private def getWorkers(tactics:TacticsOptions):String = {
    if      (tactics.has(Tactics.Workers.FightAll))   "Fight (All)"
    else if (tactics.has(Tactics.Workers.FightHalf))  "Fight (Half)"
    else if (tactics.has(Tactics.Workers.Flee))       "Flee"
    else                                              "-"
  }
  
  private def drawTacticsReport(battle:Battle) {
    drawTacticsReport(battle.bestTactics,           tableStart0, With.self.name)
    drawTacticsReport(battle.enemy.tacticsApparent, tableStart1, With.enemies.head.name)
    
    if (With.configuration.visualizeBattleTacticsRanks) {
      With.game.drawTextScreen(
        tacticsRanks.bwapi,
        battle.rankedTactics
          .zipWithIndex
          .map(pair => "#" + (pair._2 + 1) + " " + pair._1)
          .mkString("\n"))
    }
  }
  
  private def drawTacticsReport(tactics: TacticsOptions, origin:Pixel, playerName:String) {
    DrawScreen.table(
      origin.x,
      origin.y,
      Vector(
        Vector(playerName),
        Vector(""),
        Vector("Move:",     getMove(tactics)),
        Vector("Focus:",    getFocus(tactics)),
        Vector("Workers:",  getWorkers(tactics)),
        Vector("Wounded:",  getWounded(tactics))
      ))
  }
}

