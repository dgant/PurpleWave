package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Information.Battles.Estimations.Simulation.Simulacrum
import Information.Battles.Types.{Battle, Team}
import Lifecycle.With

object ShowBattleDetails extends View {
  
  override def renderScreen() {
    ShowBattles.localBattle.foreach(battle => {
      renderBattleScreen(
        battle,
        battle.estimationSimulationAttack,
        5,
        5 * With.visualization.lineHeightSmall)
    })
  }
  
  def renderBattleScreen(battle: Battle, estimation: Estimation, x: Int, y: Int) {
    val table = Vector(
      Vector[String]("", With.self.name, With.enemy.name),
      Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
      Vector[String]("Seconds", "" + estimation.frames / 24, ""),
      Vector[String]("Value", "" + estimation.costToEnemy.toInt, "" + estimation.costToUs.toInt),
      Vector[String]("Participants", "" + estimation.totalUnitsUs, "" + estimation.totalUnitsEnemy),
      Vector[String]("Survivors",
        survivorPercentage(estimation.deathsUs,    estimation.totalUnitsUs),
        survivorPercentage(estimation.deathsEnemy, estimation.totalUnitsEnemy)),
      Vector[String]("Desire", "%1.2f".format(battle.desire)))
    DrawScreen.table(x, y, table)
    
    val y2 = y + (table.length + 2) * With.visualization.lineHeightSmall
    val y3 = y2 + With.visualization.lineHeightSmall
    With.game.drawTextScreen(x, y2, describeTeam(battle.us))
    With.game.drawTextScreen(x, y3, describeTeam(battle.enemy))
  }
  
  def describeTeam(team: Team): String = {
    team.units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => p._2.size + " " + p._1).mkString(", ")
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

