package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint1ArmoryUpgrades extends Fingerprint {
  override protected def investigate: Boolean = {
    With.frame < GameTime(8, 0)() &&
      With.units.countEnemy(Terran.Factory) < 5 && (
      With.units.existsEnemy(u => u.is(Terran.Armory) && u.upgrading)
      || With.enemy.hasUpgrade(Terran.MechDamage)
      || With.enemy.hasUpgrade(Terran.MechArmor)
      || With.enemy.hasUpgrade(Terran.AirDamage)
      || With.enemy.hasUpgrade(Terran.AirArmor))
  }
  override protected def sticky: Boolean = true
}