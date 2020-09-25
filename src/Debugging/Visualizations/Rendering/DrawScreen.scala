package Debugging.Visualizations.Rendering

import Debugging.Visualizations.Colors
import Information.Battles.Prediction.PredictionLocal
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import Utilities.ByOption
import bwapi.Color

object DrawScreen {
  
  def header(x: Int, text: String) {
    column(x, With.visualization.lineHeightSmall, text)
  }

  def column(x: Int, y: Int, text: Iterable[String]) {
    column(x, y, text.mkString("\n"))
  }

  def column(x: Int, y: Int, text: String) {
    With.game.drawTextScreen(x, y, text)
  }
  def text(pixel: Pixel, text: String): Unit = {
    With.game.drawTextScreen(pixel.x, pixel.y, text)
  }
  
  def table(x: Int, y: Int, cells: Iterable[Iterable[String]]) {
    cells.zipWithIndex.foreach(pair => tableRow(x, y, pair._2, pair._1))
  }
  
  def tableRow(x: Int, y: Int, rowIndex: Int, row: Iterable[String]) {
    row.zipWithIndex.foreach(pair =>
      With.game.drawTextScreen(
        x + pair._2 * 60,
        y + rowIndex * 13,
        pair._1))
  }
  
  case class GraphCurve(color: Color, points: IndexedSeq[Double])
  
  def graph(
    start:    Pixel,
    label:    String,
    curves:   Seq[GraphCurve],
    fixedYMin: Option[Double] = None,
    fixedYMax: Option[Double] = None,
    fixedXMax: Option[Int] = None,
    color0:   Color = Colors.DeepViolet,
    color1:   Color = Colors.MidnightViolet,
    width:    Int = 90,
    height:   Int = 90 + With.visualization.lineHeightSmall,
    margin:   Int = 2) {

    val end               = start.add(width, height)
    val innerBorderStart  = start             .add      (margin, margin + With.visualization.lineHeightSmall)
    val innerBorderEnd    = end               .subtract (margin, margin)
    val pointStart        = innerBorderStart  .add      (margin, margin)
    val pointEnd          = innerBorderEnd    .subtract (margin, margin)

    if (curves.flatten(_.points).isEmpty) {
      return
    }

    val dataMin = curves.flatten(_.points).min
    val dataMax = curves.flatten(_.points).max
    val yMin    = Math.min(dataMin, fixedYMin.getOrElse(dataMin))
    val yMax    = Math.max(dataMax, fixedYMax.getOrElse(dataMax))
    val xMax    = fixedXMax.getOrElse(curves.map(_.points.size).max)
    val scaleX  = (pointEnd.x - pointStart.x) / xMax.toDouble
    val scaleY  = (pointEnd.y - pointStart.y) / Math.max(1.0, yMax - yMin)
    
    With.game.drawBoxScreen(start.bwapi, end.bwapi, color0, true)
    With.game.drawBoxScreen(innerBorderStart.bwapi, innerBorderEnd.bwapi, color1, true)
    With.game.drawTextScreen(start.add(margin, 0).bwapi, label)
    
    curves.foreach(curve => {
      var i = 0
      while (i < Math.min(xMax, curve.points.size) - 1) {
        With.game.drawLineScreen(
          (pointStart.x + scaleX * i                         ).toInt,
          (pointStart.y + scaleY * (yMax - curve.points(i  ))).toInt,
          (pointStart.x + scaleX * (i + 1)                   ).toInt,
          (pointStart.y + scaleY * (yMax - curve.points(i+1))).toInt,
          curve.color)
        i += 1
      }
    })
  }

  private def drawBattleMap(battle: BattleLocal, estimation: PredictionLocal) {
    val weWin               = battle.judgement.exists(_.shouldFight)
    val ourColorDark        = With.self.colorMidnight
    val enemyColorDark      = With.enemy.colorMidnight
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

  }
}
