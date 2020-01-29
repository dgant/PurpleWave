package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Terran

class FingerprintBio extends Fingerprint {
  override protected def investigate: Boolean = {
    if (With.units.countEnemy(Terran.Barracks) > 2) {
      return true
    }
    if (With.units.countEnemy(Terran.Barracks) > 1 && ! With.fingerprints.bbs.matches) {
      return true
    }

    if (With.intelligence.unitsShown.allEnemies(Terran.Vulture) < 4) {
      if (With.enemies.exists(_.hasUpgrade(Terran.MarineRange))) {
        return true
      }
      if (With.enemies.exists(_.hasTech(Terran.Stim))) {
        return true
      }
      if (With.intelligence.unitsShown.allEnemies(Terran.Medic) > 0) {
        return true
      }
      if (With.intelligence.unitsShown.allEnemies(Terran.Firebat) > 0) {
        return true
      }
      if (With.intelligence.unitsShown.allEnemies(Terran.Marine) > 8) {
        return true
      }
      if (With.units.existsEnemy(u => u.is(Terran.Academy) && u.upgrading)) {
        return true
      }
    }

    val enemyMech = (
        2 * With.units.countEnemy(UnitMatchSiegeTank)
      + 3 * With.units.countEnemy(Terran.Battlecruiser)
      + With.units.countEnemy(Terran.Vulture)
      + With.units.countEnemy(Terran.Goliath)
      + With.units.countEnemy(Terran.Wraith)
    )
    val enemyBio = ((
      With.units.countEnemy(Terran.Marine)
      + 2 * With.units.countEnemy(Terran.Firebat)
      + 2 * With.units.countEnemy(Terran.Medic)
    )
      * (if (With.enemies.exists(_.hasUpgrade(Terran.MarineRange))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasUpgrade(Terran.BioDamage))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasUpgrade(Terran.BioArmor))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasTech(Terran.Stim))) 2.0 else 1.0)
    )
    enemyBio > Math.max(8, enemyMech)
  }

  override protected def sticky: Boolean = With.frame > GameTime(10, 0)()
}
