package Strategery.Selection
import Lifecycle.With
import Strategery.Maps.{Gladiator, Sparkle, ThirdWorld, Transistor}
import Strategery.Strategies.Strategy
import Strategery.Strategies.Zerg._
import Utilities.ByOption

object StrategySelectionAIST1 extends StrategySelectionPolicy {
  
  override def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    
    if (Sparkle.matches) {
      return Iterable(ZergSparkle)
    }
  
    var allowed: Vector[Strategy] = topLevelStrategies.toVector
    
    if (With.enemy.isZerg) {
      allowed = Vector(NineHatchLings, NinePoolMuta)
      
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
        allowed = Vector(ZvPNinePool)
      }
      else if (ThirdWorld.matches) {
        allowed = Vector(ZvPNinePool)
      }
      else if (Transistor.matches) {
        allowed = Vector(ZvPNinePool)
      }
    }
    
    val output = StrategySelectionFree.chooseBest(allowed)
    output
  }
  
}
