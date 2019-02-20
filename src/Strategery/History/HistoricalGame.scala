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
  strategies      : Set[String],
  var order       : Int = 0) {
  
  // Convenience methods
  def weight        : Double = With.strategy.gameWeights(this)
  def winsWeighted  : Double = if (won) weight else 0.0

  def weEmployed(strategy: Strategy): Boolean = {
    strategies.contains(strategy.toString)
  }

  override def toString: String = (
    (if (won) "W" else "L") + ": "
    + ourRace.toString.take(1) + "v" + enemyRace.toString.take(1) + " "
    + enemyName + " "
    + "@ " + mapName
    + " (" + strategies.map(_.toString).mkString(", ") + ")"
  )
}
