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
      
      val desperation         = Math.max(1.0, 24.0 / unit.distance(With.geography.home))
      val groupStrengthUs     = 0.01 + intent.battle.get.us.strength
      val groupStrengthEnemy  = 0.01 + intent.battle.get.enemy.strength
      val localStrengthUs     = 0.01 + With.grids.friendlyGroundStrength.get(unit.position.toTilePosition)
      val localStrengthEnemy  = 0.01 + With.grids.enemyGroundStrength.get(unit.position.toTilePosition)
      val healthFactor = 0.5 + 0.5 * unit.totalHealth / unit.maxTotalHealth
      val groupMotivation = groupStrengthUs / groupStrengthEnemy
      val localMotivation = intent.motivation * desperation * healthFactor * groupMotivation * localStrengthUs / localStrengthEnemy
  
      intent.motivation = localMotivation
      
      if (groupMotivation < 1) {
        Flee.execute(intent)
      }
      else if (groupMotivation < 1) {
        if (localMotivation < 0.5) {
          Flee.execute(intent)
        }
        else if (localMotivation < 1) {
          Approach.execute(intent)
        }
        else {
          Skirt.execute(intent)
        }
      }
      else {
        if (localMotivation < 1) {
          Skirt.execute(intent)
        }
        else {
          Engage.execute(intent)
        }
      }
    }
  }
}
