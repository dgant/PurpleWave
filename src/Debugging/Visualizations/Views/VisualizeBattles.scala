package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen.GraphCurve
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.{Colors, Visualization}
import Information.Battles.BattleTypes.Battle
import Information.Battles.Estimation.{BattleEstimationResult, BattleEstimationState}
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Pixels.Pixel
import Planning.Yolo
import Utilities.EnrichPixel._

object VisualizeBattles {
  
  private val graphMargin             = Pixel(2, 2)
  private val graphWidth              = 90
  private val graphHeight             = 90 + Visualization.lineHeightSmall
  private val valueGraph              = graphMargin
  private val healthGraph             = valueGraph.add(0, graphHeight + graphMargin.y)
  private val positionGraph           = healthGraph.add(0, graphHeight + graphMargin.y)
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
    val neutralColor        = Colors.NeonOrange
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
  
    DrawScreen.graph(
      valueGraph,
      "Value:",
      Vector(
        new GraphCurve(With.self.colorNeon,         estimation.statesUs.map(stateValue)),
        new GraphCurve(With.enemies.head.colorNeon, estimation.statesUs.map(stateValue))))
    
    DrawScreen.graph(
      healthGraph,
      "Health:",
      Vector(
        new GraphCurve(With.self.colorNeon,         estimation.statesUs.map(stateHealth)),
        new GraphCurve(With.enemies.head.colorNeon, estimation.statesUs.map(stateHealth))))
    
    DrawScreen.graph(
      positionGraph,
      "Position:",
      Vector(
        new GraphCurve(With.self.colorNeon,         estimation.statesUs.map(_.pixelsAway)),
        new GraphCurve(With.enemies.head.colorNeon, estimation.statesUs.map(_.pixelsAway))))
  }
  private def stateValue  (state: BattleEstimationState):Double = state.avatar.subjectiveValue * (state.avatar.totalHealth - state.damageReceived)
  private def stateHealth (state: BattleEstimationState):Double = state.avatar.totalHealth - state.damageReceived
  
  
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

