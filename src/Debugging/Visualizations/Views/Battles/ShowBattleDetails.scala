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
    ShowBattleSummary.localBattle.foreach(battle => {
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
      Vector[String]("Gains",   "%1.2f".format(battle.analysis.attackGains)),
      Vector[String]("Losses",  "%1.2f".format(battle.analysis.attackLosses)),
      Vector[String]("Desire",  "%1.2f".format(battle.desire)))
    DrawScreen.table(x, y, table)
    
    val y2 = y + (table.length + 4) * With.visualization.lineHeightSmall
    DrawScreen.table(5, y2,
      Vector(
        Vector("", "Participants"),
        Vector(With.self.name,  describeTeam(battle.us.units)),
        Vector(With.enemy.name, describeTeam(battle.enemy.units)),
        Vector(),
        Vector("", "Survivors"),
        Vector(With.self.name,  describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filterNot(_._2.dead).keys)),
        Vector(With.enemy.name, describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filterNot(_._2.dead).keys)),
        Vector(),
        Vector("", "Deaths"),
        Vector(With.self.name,  describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filter(_._2.dead).keys)),
        Vector(With.enemy.name, describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filter(_._2.dead).keys))
    ))
    
    val x2 = 200
    val valueUsInitial    = battle.us.units.map(_.subjectiveValue).sum
    val valueEnemyInitial = battle.enemy.units.map(_.subjectiveValue).sum
    val valueUsLost       = estimation.costToUs.toInt
    val valueEnemyLost    = estimation.costToEnemy.toInt
    val valueUsKept       = Math.max(0, valueUsInitial - valueUsLost)
    val valueEnemyKept    = Math.max(0, valueEnemyInitial - valueEnemyLost)
    val baseWidth         = 200
    val denominator       = Array(1, valueUsInitial, valueEnemyInitial).max
    val dxUsInitial       = baseWidth * valueUsInitial    / denominator
    val dxEnemyInitial    = baseWidth * valueEnemyInitial / denominator
    val dxUsLost          = baseWidth * valueUsLost       / denominator
    val dxEnemyLost       = baseWidth * valueEnemyLost    / denominator
    val dxUsKept          = baseWidth * valueUsKept       / denominator
    val dxEnemyKept       = baseWidth * valueEnemyKept    / denominator
    var nextY             = y
    val boxHeight         = With.visualization.lineHeightSmall + 4
    val margin            = 2
    
    val nameUs      = With.self.name
    val nameEnemy   = With.enemy.name
    val colorUs     = With.self.colorMedium
    val colorEnemy  = With.enemy.colorMedium
    drawBar(x2, nextY, dxUsInitial,     boxHeight, colorUs,     "Start (" + nameUs    + ")")  ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyInitial,  boxHeight, colorEnemy,  "Start (" + nameEnemy + ")")  ; nextY += boxHeight + margin
    drawBar(x2, nextY, dxUsKept,        boxHeight, colorUs,     "Kept ("  + nameUs    + ")")  ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyKept,     boxHeight, colorEnemy,  "Kept ("  + nameEnemy + ")")  ; nextY += boxHeight + margin
    drawBar(x2, nextY, dxUsLost,        boxHeight, colorUs,     "Lost ("  + nameUs    + ")")  ; nextY += boxHeight
    drawBar(x2, nextY, dxEnemyLost,     boxHeight, colorEnemy,  "Lost ("  + nameEnemy + ")")
    
    val scoreTable = Vector(
      Vector("Attack secs",     "" + battle.estimationSimulationAttack.frames / 24),
      Vector("Attack gain",     "" + battle.estimationSimulationAttack.costToEnemy.toInt),
      Vector("Attack loss",     "" + battle.estimationSimulationAttack.costToUs.toInt),
      Vector(),
      Vector("Retreat secs",    "" + battle.estimationSimulationRetreat.frames / 24),
      Vector("Retreat gain",    "" + battle.estimationSimulationRetreat.costToEnemy.toInt),
      Vector("Retreat loss",    "" + battle.estimationSimulationRetreat.costToUs.toInt),
      Vector(),
      Vector("DChokiness",      "" + "%1.1f".format(battle.analysis.desireChokiness)),
      Vector("DEconomy",        "" + "%1.1f".format(battle.analysis.desireEconomy)),
      Vector("DTurtling",       "" + "%1.1f".format(battle.analysis.desireTurtling)),
      Vector("DUrgency",        "" + "%1.1f".format(battle.analysis.desireUrgency)),
      Vector("DHysteresis",     "" + "%1.1f".format(battle.analysis.desireHysteresis)),
      Vector("DMultiplier",     "" + "%1.1f".format(battle.analysis.desireMultiplier)),
      Vector(),
      Vector("DTotal",          "" + "%1.1f".format(battle.desire))
    )
    DrawScreen.table(450, y, scoreTable)
  }
  
  def drawBar(x: Int, y: Int, dx: Int, dy: Int, color: Color, label: String) {
    With.game.drawBoxScreen(x, y, x+dx, y+dy, color, true)
    With.game.drawTextScreen(x + 2, y + 1, label)
  }
  
  def describeTeam(units: Iterable[UnitInfo]): String = {
    units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => p._2.size + " " + p._1).mkString(", ")
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%%"
  }
  
  override def renderMap() {
    ShowBattleSummary.localBattle.foreach(renderBattleMap)
  }
  
  def renderBattleMap(battle: Battle) {
    val simulation = battle.estimationSimulationAttack.simulation.get
    simulation.simulacra.values.foreach(renderSimulacrumMap)
  }
  
  def renderSimulacrumMap(sim: Simulacrum) {
    val attacking = sim.simulation.weAttack
    val color = if (attacking) sim.unit.player.colorNeon else sim.unit.player.colorMidnight
    
    sim.moves.foreach(move => DrawMap.arrow(move._1, move._2, color))
    if (sim.dead) {
      DrawMap.circle(
        sim.pixel,
        5,
        if (attacking) Colors.NeonOrange else Colors.MidnightOrange,
        solid = true)
    }
  }
}

