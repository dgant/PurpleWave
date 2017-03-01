package Global.Combat.Battle

import bwapi.Position
import Utilities.Enrichment.EnrichPosition._

class Battle(
  val us:BattleGroup,
  val enemy:BattleGroup) {
  
  def focus:Position = us.vanguard.midpoint(enemy.vanguard)
}
