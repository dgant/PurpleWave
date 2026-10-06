package ProxyBwapi.UnitInfo

// An immutable attack description, safe to carry from the game thread into simulation.
final case class DamageVolley(
  damagePerHit: Double,
  hits: Int,
  armorHealth: Double,
  armorShield: Double,
  healthMultiplier: Double,
  hitChance: Double = 1.0) {

  def apply(shields: Double): DamageDealt = {
    var shieldsLeft = Math.max(0.0, shields)
    var healthDamage = 0.0
    var shieldDamage = 0.0
    var hit = 0
    while (hit < hits) {
      var damage = Math.max(0.5, damagePerHit)
      // Brood War only applies shield armor/absorption with at least one shield point.
      val absorbed = if (shieldsLeft >= 1.0) {
        damage = Math.max(0.5, damage - armorShield)
        Math.min(shieldsLeft, damage)
      } else 0.0
      shieldsLeft -= absorbed
      shieldDamage += absorbed
      val hull = Math.max(0.0, damage - absorbed - armorHealth) * healthMultiplier
      healthDamage += (if (absorbed == 0.0) Math.max(0.5, hull) else hull)
      hit += 1
    }
    DamageDealt(healthDamage * hitChance, shieldDamage * hitChance)
  }
}

final case class DamageDealt(health: Double, shields: Double) {
  def total: Double = health + shields
}
