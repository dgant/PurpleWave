package Strategery.Selection
import Lifecycle.With
import Strategery.Maps.{Gladiator, Sparkle, ThirdWorld, Transistor}
import Strategery.Strategies.Strategy
import Strategery.Strategies.Zerg._
import Utilities.ByOption

object StrategySelectionAIST1 extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
  
    var allowed: Vector[Strategy] = topLevelStrategies.toVector
    
    if (With.enemy.isZerg) {
      allowed = allowed.filter(strategy => Vector(ZergSparkle, NineHatchLings, NinePoolMuta).contains(strategy))
      
      val lastGame = ByOption.maxBy(With.history.games.filter(_.enemyName == With.enemy.name))(_.timestamp)
      if (lastGame.exists(_.strategies.contains(NineHatchLings.toString))) {
        allowed = allowed.filterNot(_ == NineHatchLings)
      }
      else {
        allowed = allowed.filterNot(_ == NinePoolMuta)
      }
    }
    else if (With.enemy.isProtoss) {
      if (Sparkle.matches) {
        allowed = Vector(ZergSparkle)
      }
      else if (Gladiator.matches) {
        allowed = Vector(ZvPTwoHatchMuta)
      }
      else if (ThirdWorld.matches) {
        allowed = Vector(ZvPNinePoolThreeHatch)
      }
      else if (Transistor.matches) {
        allowed = Vector(ZvPNinePoolThreeHatch)
      }
    }
    
    val output = StrategySelectionFree.chooseBest(allowed)
    output
  }
  
}
