package Global.Combat.Commands
import Startup.With
import Types.Intents.Intention

object Control extends Command {
  
  def execute(intent: Intention) {
    val unit = intent.unit
    
    intent.battle = With.battles.all.find(_.us.units.contains(unit))
  
    if (intent.battle.isEmpty) {
      Pillage.execute(intent)
    } else if (unit.cloaked && ! intent.battle.get.enemy.units.exists(_.utype.isDetector)) {
      Engage.execute(intent)
    } else {
      
      val desperation         = Math.max(1.0, 32.0 * 32 / With.paths.groundDistance(unit.tileCenter, With.geography.home))
      val groupStrengthUs     = 0.01 + intent.battle.get.us.strength
      val groupStrengthEnemy  = 0.01 + intent.battle.get.enemy.strength
      val localStrengthUs     = 0.01 + With.grids.friendlyGroundStrength.get(unit.position.toTilePosition)
      val localStrengthEnemy  = 0.01 + With.grids.enemyGroundStrength.get(unit.position.toTilePosition)
      val groupConfidence     = groupStrengthUs / groupStrengthEnemy
      val localConfidence     = localStrengthUs / localStrengthEnemy
      val healthConfidence    = 0.2 + 0.8 * unit.totalHealth / unit.maxTotalHealth
      val localMotivation     = intent.motivation * desperation * healthConfidence  * groupConfidence * localConfidence
  
      intent.motivation = localMotivation
      
      if (intent.motivation < 0.5) {
        Flee.execute(intent)
      }
      else if (intent.motivation < 0.8) {
        Approach.execute(intent)
      }
      else if (intent.motivation < 1.2) {
        Skirt.execute(intent)
      }
      else {
        Engage.execute(intent)
      }
    }
  }
}
