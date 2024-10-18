package Strategery.History

import Lifecycle.With
import Strategery.Strategies.Strategy
import Utilities.?
import bwapi.Race

case class HistoricalGame(
  timestamp       : Long,
  startLocations  : Int,
  mapName         : String,
  enemyName       : String,
  ourRace         : Race,
  enemyRace       : Race,
  won             : Boolean,
  tags            : Seq[String]) {

  def weight: Double = With.strategy.gameWeights.getOrElse(this, 0.0000001)

  def weEmployed(strategy: Strategy): Boolean = tags.contains(strategy.toString)

  lazy val enemyMatches: Boolean = enemyName == With.history.currentEnemyName

  override def toString: String = {
    val outcome = ?(won, "W", "L")
    f"$outcome: ${ourRace.toString.take(1)}v${enemyRace.toString.take(1)} $enemyName @ $mapName (${tags.mkString(", ")})"  }
}
