package Strategery.Strategies

import Lifecycle.With
import Mathematics.Maff
import Performance.GameCache
import Strategery.Selection.{WeightedGame, WinProbability}

case class StrategyBranch(strategies: Seq[Strategy]) {

  override def toString: String = strategies.map(_.toString).mkString(" + ")

  def weightedGames     : Vector[WeightedGame]  = _weightedGames()
  def winProbability    : Double                = _winProbability()
  def explorationWeight : Double                = _explorationWeight()
  def explorationGames  : Double                = _explorationGames()

  private val _weightedGames     = new GameCache(() => With.strategy.gamesVsOpponent.filter(game => strategies.exists(game.weEmployed)).map(new WeightedGame(this, _)).toVector)
  private val _winProbability    = new GameCache(() => WinProbability(this))
  private val _explorationWeight = new GameCache(() => Maff.nanToOne(1.0 / Math.sqrt(strategies.length))) // Surely we can come up with something more principled than this
  private val _explorationGames  = new GameCache(() => Math.sqrt(200) * explorationWeight / With.strategy.strategyBranchesLegal.map(_.explorationWeight).sum)

  def legal: Boolean = strategies.forall(_.legal)
}
