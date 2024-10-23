package Macro.Actions

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.Seconds

object SneakyCitadel extends MacroActions {
  def apply(): Unit = {
    if (scoutCleared) {
      get(Protoss.CitadelOfAdun)
      cancel(Protoss.AirDamage)
    } else if (units(Protoss.CitadelOfAdun) == 0) {
      if (With.units.ours.find(_.upgradeProducing.contains(Protoss.AirDamage)).exists(_.remainingUpgradeFrames < Seconds(5)())) {
        cancel(Protoss.AirDamage)
      } else if ( ! upgradeStarted(Protoss.DragoonRange)) {
        get(Protoss.AirDamage)
      }
    }
  }
}
