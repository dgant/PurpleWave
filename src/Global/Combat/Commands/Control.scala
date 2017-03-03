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
      val groupStrengthUs     = 0.01 + intent.battle.get.us.strength
      val groupStrengthEnemy  = 0.01 + intent.battle.get.enemy.strength
      val localStrengthUs     = With.grids.friendlyGroundStrength.get(unit.position.toTilePosition)
      val localStrengthEnemy  = With.grids.enemyGroundStrength.get(unit.position.toTilePosition)
      
      val strengthFactor =  0.5 + unit.totalHealth.toDouble / unit.maxTotalHealth / 2
      val groupConfidence = groupStrengthUs / groupStrengthEnemy
      val localConfidence = strengthFactor * localStrengthUs / localStrengthEnemy

      if (groupConfidence < 1) {
        if (localConfidence < 1) {
          Approach.execute(intent)
        }
        else if (localConfidence < 1) {
          Skirt.execute(intent)
        }
        else {
          Engage.execute(intent)
        }
      }
      else if (groupConfidence < 2) {
        if (localConfidence < 1) {
          Skirt.execute(intent)
        }
        else {
          Engage.execute(intent)
        }
      }
      else {
        Engage.execute(intent)
      }
    }
  }
}
