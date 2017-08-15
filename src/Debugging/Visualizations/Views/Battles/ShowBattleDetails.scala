package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Information.Battles.Estimations.Simulation.Simulacrum
import Information.Battles.Types.Battle
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowBattleDetails extends View {
  
  override def renderScreen() {
    ShowBattles.localBattle.foreach(battle => {
      renderBattleScreen(
        battle,
        battle.estimationSimulationAttack)
    })
  }
  
  def renderBattleScreen(battle: Battle, estimation: Estimation) {
    val x = 5
    val y = 7 * With.visualization.lineHeightSmall
    val table = Vector(
      Vector[String]("", With.self.name, With.enemy.name),
      Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
      Vector[String]("Seconds",       "" + estimation.frames / 24,        ""),
      Vector[String]("Value",         "" + estimation.costToEnemy.toInt,  "" + estimation.costToUs.toInt),
      Vector[String]("Participants",  "" + estimation.totalUnitsUs,       "" + estimation.totalUnitsEnemy),
      Vector[String]("Survivors",
        survivorPercentage(estimation.deathsUs,    estimation.totalUnitsUs),
        survivorPercentage(estimation.deathsEnemy, estimation.totalUnitsEnemy)),
      Vector[String]("Desire", "%1.2f".format(battle.desire)))
    DrawScreen.table(x, y, table)
    
    val y2 = y + (table.length + 4) * With.visualization.lineHeightSmall
    DrawScreen.column(5, y2,
      Vector(
      "Participants",
      describeTeam(battle.us.units),
      describeTeam(battle.enemy.units),
      "",
      "Survivors",
      describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filterNot(_._2.dead).keys),
      describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filterNot(_._2.dead).keys),
      "",
      "Deaths",
      describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filter(_._2.dead).keys),
      describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filter(_._2.dead).keys)
    ))
    
    val x2 = 200
    val valueUsInitial    = battle.us.units.map(_.subjectiveValue).sum
    val valueEnemyInitial = battle.us.units.map(_.subjectiveValue).sum
    val valueUsLost       = estimation.costToUs.toInt
    val valueEnemyLost    = estimation.costToEnemy.toInt
    val valueUsKept       = Math.max(0, valueUsInitial - valueUsLost)
    val valueEnemyKept    = Math.max(0, valueEnemyInitial - valueEnemyLost)
    val baseline          = 200 / Array(1, valueUsInitial, valueEnemyInitial).max
    val dxUsInitial       = valueUsInitial    / baseline
    val dxEnemyInitial    = valueEnemyInitial / baseline
    val dxUsLost          = valueUsLost       / baseline
    val dxEnemyLost       = valueEnemyLost    / baseline
    val dxUsKept          = valueUsKept       / baseline
    val dxEnemyKept       = valueEnemyKept    / baseline
    var nextY             = y
    val boxHeight         = With.visualization.lineHeightSmall
    val margin            = 2
    
    val colorUs     = With.self.colorMedium
    val colorEnemy  = With.enemy.colorMedium
    drawBar(x2, nextY, dxUsInitial,     boxHeight, colorUs,     "Start (Us)")     ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyInitial,  boxHeight, colorEnemy,  "Start (Enemy)")  ; nextY += boxHeight + margin
    drawBar(x2, nextY, dxUsKept,        boxHeight, colorUs,     "Kept (Us)")      ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyKept,     boxHeight, colorEnemy,  "Kept (Enemy)")   ; nextY += boxHeight + margin
    drawBar(x2, nextY, dxUsLost,        boxHeight, colorUs,     "Lost (Us)")      ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyLost,     boxHeight, colorEnemy,  "Lost (Enemy)")
  }
  
  def drawBar(x: Int, y: Int, dx: Int, dy: Int, color: Color, label: String) {
    With.game.drawBoxScreen(x, y, x+dx, y+dy, color)
    With.game.drawTextScreen(x + 1, y + 1, label)
  }
  
  def describeTeam(units: Iterable[UnitInfo]): String = {
    units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => p._2.size + " " + p._1).mkString(", ")
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%%"
  }
  
  override def renderMap() {
    ShowBattles.localBattle.foreach(renderBattleMap)
  }
  
  def renderBattleMap(battle: Battle) {
    val simulation = battle.estimationSimulationAttack.simulation.get
    simulation.simulacra.values.foreach(renderSimulacrumMap)
  }
  
  def renderSimulacrumMap(sim: Simulacrum) {
    sim.moves.foreach(move => DrawMap.arrow(move._1, move._2, sim.unit.player.colorMedium))
    if (sim.dead) {
      DrawMap.circle(sim.pixel, 5, Colors.NeonOrange)
    }
  }
}

