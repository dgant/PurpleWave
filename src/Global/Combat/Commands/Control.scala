package Global.Combat.Commands
import Global.Combat.Battle.BattleMetrics
import Startup.With
import Types.Intents.Intention

object Control extends Command {
  
  def execute(intent: Intention) {
    val unit = intent.unit
    
    intent.battle = With.battles.all.find(_.us.units.contains(unit))
  
    if (intent.battle.isEmpty) {
      March.execute(intent)
    } else if (unit.cloaked && ! intent.battle.get.enemy.units.exists(_.utype.isDetector)) {
      Fight.execute(intent)
    } else {
      val groupStrengthUs     = 0.01 + intent.battle.get.us.strength
      val groupStrengthEnemy  = 0.01 + intent.battle.get.enemy.strength
      val localStrengthUs     = 0.01 + intent.battle.get.us.units.view.map(otherUnit => BattleMetrics.evaluate(otherUnit, unit.position)).sum
      val localStrengthEnemy  = 0.01 + intent.battle.get.enemy.units.view.map(otherUnit => BattleMetrics.evaluate(otherUnit, unit.position)).sum
      
      val groupConfidence = groupStrengthUs / groupStrengthEnemy
      val localConfidence = localStrengthUs / localStrengthEnemy
  
      if (groupConfidence < 1) {
        if (localConfidence < 1) {
          Avoid.execute(intent)
        }
        else {
          Reenforce.execute(intent)
        }
      }
      else {
        if (localConfidence < 1) {
          Reenforce.execute(intent)
        }
        else {
          Fight.execute(intent)
        }
      }
    }
  }
}
