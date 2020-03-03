package Strategery.History

import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

case class HistoricalGame(
                           timestamp       : Long,
                           startLocations  : Int,
                           mapName         : String,
                           enemyName       : String,
                           ourRace         : Race,
                           enemyRace       : Race,
                           won             : Boolean,
                           tags      : Seq[String]) {
  
  // Convenience methods
  def weight        : Double = With.strategy.gameWeights.getOrElse(this, 0.0000001)
  def winsWeighted  : Double = if (won) weight else 0.0

  def weEmployed(strategy: Strategy): Boolean = tags.contains(strategy.toString)

  lazy val enemyMatches: Boolean = enemyName == With.history.currentEnemyName

  override def toString: String = (
    (if (won) "W" else "L") + ": "
    + ourRace.toString.take(1) + "v" + enemyRace.toString.take(1) + " "
    + enemyName + " "
    + "@ " + mapName
    + " (" + tags.map(_.toString).mkString(", ") + ")"
  )
}
