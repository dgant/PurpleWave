package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Information.Battles.Prediction.Prediction
import Information.Battles.Prediction.Simulation.Simulacrum
import Information.Battles.Types.BattleLocal
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
  
  def renderBattleScreen(battle: BattleLocal, estimation: Prediction) {
    val barHeight       = With.visualization.lineHeightSmall
    val x               = 5
    var y0              = 4 * With.visualization.lineHeightSmall
    var y1              = y0 + barHeight
    var y2              = y1 + 5
    var y3              = y2 + barHeight
    val y4              = y3 + 5
    val denominator     = 630 * With.battles.global.valueUsArmy + With.battles.global.valueEnemyArmy
    val xGlobalUs       = (With.battles.global.valueUsArmyKnown    / denominator).toInt
    val xGlobalEnemy    = (With.battles.global.valueEnemyArmy / denominator).toInt
    
    DrawScreen.table(x, y4, Vector(
      Vector("Global",      "%1.2f".format(With.battles.global.valueRatioTarget)),
      Vector("Attack",      "%1.2f".format(battle.ratioAttack)),
      Vector("Snipe",       "%1.2f".format(battle.ratioSnipe)),
      Vector("Target",      "%1.2f".format(battle.ratioTarget)),
      Vector("Hysteresis",  "%1.2f".format(battle.hysteresis)),
      Vector("Urgency",     "%1.2f".format(battle.urgency))
    ))
  }
  
  def renderBattleScreenOld(battle: BattleLocal, estimation: Prediction) {
    val x = 5
    val y = 4 * With.visualization.lineHeightSmall
    val table = Vector(
      Vector[String]("", With.self.name, With.enemy.name),
      Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
      Vector[String]("Seconds",       "" + estimation.frames / 24,        ""),
      Vector[String]("Value",         "" + estimation.costToEnemy.toInt,  "" + estimation.costToUs.toInt),
      Vector[String]("Participants",  "" + estimation.totalUnitsUs,       "" + estimation.totalUnitsEnemy),
      Vector[String]("Survivors",
        survivorPercentage(estimation.deathsUs,    estimation.totalUnitsUs),
        survivorPercentage(estimation.deathsEnemy, estimation.totalUnitsEnemy)),
      Vector[String]("Gains",   "%1.2f".format(battle.attackGains)),
      Vector[String]("Losses",  "%1.2f".format(battle.attackLosses)),
      Vector[String]("Net",     "%1.2f".format(battle.attackGains - battle.attackLosses)))
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
      Vector("Attack loss",     "" + battle.estimationSimulationAttack.costToUs.toInt)
    )
    DrawScreen.table(450, y, scoreTable)
  }
  
  def drawBar(x: Int, y: Int, dx: Int, dy: Int, color: Color, label: String = "") {
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
  
  def renderBattleMap(battle: BattleLocal) {
    val simulation = battle.estimationSimulationAttack.simulation.get
    simulation.simulacra.values.foreach(renderSimulacrumMap)
  }
  
  def renderSimulacrumMap(sim: Simulacrum) {
    val attacking = sim.simulation.weAttack
    val color = if (attacking) sim.realUnit.player.colorNeon else sim.realUnit.player.colorMidnight
    
    sim.events.foreach(_.draw())
    if (sim.dead) {
      val dark = sim.realUnit.player.colorMidnight
      val skullColor = sim.realUnit.player.colorNeon
      DrawMap.box(sim.pixel.add(-3, 0), sim.pixel.add(4, 8), dark, solid = false)
      DrawMap.circle(sim.pixel, 5, skullColor,  solid = true)
      DrawMap.circle(sim.pixel, 5, dark,        solid = false)
      DrawMap.box(sim.pixel.add(-2,  1), sim.pixel.add(2, 7), skullColor,  solid = true)
      DrawMap.box(sim.pixel.add(-2, -2), sim.pixel.add(0, 0), Color.Black, solid = true)
      DrawMap.box(sim.pixel.add(1,  -2), sim.pixel.add(3, 0), Color.Black, solid = true)
      DrawMap.line(sim.pixel.add(-1, 3), sim.pixel.add(-2, 9), dark)
      DrawMap.line(sim.pixel.add(1,  3), sim.pixel.add(2,  9), dark)
    }
  }
}

