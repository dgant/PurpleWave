
package Planning.Plans.Army

import Planning.Plan
import Planning.Plans.Gameplans.All.MacroActions

class AttackAndHarass extends Plan with MacroActions {
  override def onUpdate(): Unit = {
    attack()
    harass()
  }
}
