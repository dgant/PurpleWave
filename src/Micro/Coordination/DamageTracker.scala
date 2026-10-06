package Micro.Coordination

import Lifecycle.With
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.Damage
import ProxyBwapi.UnitInfo.{DamageVolley, UnitInfo}
import bwapi.BulletType

final class DamageTracker {

  // A lot of the ideas here are borrowed from Stardust's damage tracking

  // Bullets which travel in the air before doing damage,
  // and are mostly sure to hit (thereby excluding Valkyries and Lurkers)
  lazy val hangtime: Set[BulletType] = Set(
    BulletType.ATS_ATA_Laser_Battery,   // Battlecruiser
    BulletType.Burst_Lasers,            // Wraith air-to-ground
    BulletType.Gemini_Missiles,         // Wraith/Goliath *-to-air
    BulletType.Fragmentation_Grenade,   // Vulture
    BulletType.Longbolt_Missile,        // Missile Turret
    BulletType.Anti_Matter_Missile,     // Scout air-to-air
    BulletType.Pulse_Cannon,            // Interceptors
    BulletType.Phase_Disruptor,         // Dragoon
    BulletType.STA_STS_Cannon_Overlay,  // Bruce thinks this is Photon Cannon and I'm guessing he's right.
    BulletType.Glave_Wurm,              // Mutalisk
    BulletType.Corrosive_Acid_Shot,     // Devourer
    BulletType.Seeker_Spores)           // Spore Colony

  var updatedFrame: Int = 0
  private var bulletSources = Map.empty[Int, UnitInfo]
  private var sourcesWithBullets = Set.empty[UnitInfo]

  def update(): Unit = {
    With.units.all.foreach(_.clearDamage())
    val bullets = With.bullets.all
    val bulletIds = bullets.map(_.id).toSet
    bulletSources = bulletSources.filter { case (id, _) => bulletIds.contains(id) }
    sourcesWithBullets = Set.empty
    bullets.foreach(countBullet)
    With.units.ours.foreach(countDamage)
    With.units.enemy.foreach(countDamage)
    updatedFrame = With.frame
  }

  @inline def countBullet(bullet: BulletInfo): Unit = {
    // Keep the source even if it dies while its projectile is still in flight.
    val sourceOption = bullet.sourceUnit.orElse(bulletSources.get(bullet.id)).orElse {
      Option(bullet.bwapi.getSource).flatMap(source => With.units.dead.find(_.id == source.getID))
    }
    if (sourceOption.isEmpty || bullet.targetUnit.isEmpty || ! bullet.exists) return
    val source = sourceOption.get
    val target = bullet.targetUnit.get
    if ( ! target.isEnemyOf(source)) return
    bulletSources += bullet.id -> source
    sourcesWithBullets += source
    if ( ! bullet.moving) return
    val bulletType = bullet.bulletType
    // Parasite or Spawn Broodling (Ensnare has its own type)
    // Queen_Spell_Carrier is shared by Parasite and Broodlings; it cannot justify a guaranteed kill.
    if ( ! hangtime.contains(bulletType) && bulletType != BulletType.Yamato_Gun) return
    // Ankmairdor hypothesizes that you can predict missed bullets before arrival: target position != target's position
    // Ank: Bullets update after units. If a moving bullet is not a miss, then it updates its target position every frame. Not all bullets move, so you would likely need to compare on the first frame of the bullet's life. Note, Valkyrie don't aim at their targets and that is not technically a miss.
    // Apply this test only to the tracking bullets above.
    if (bullet.targetPixel.exists(_ != target.pixel)) return
    val inFrames = if (bullet.speed > 0.0) {
      Math.max(1, Math.ceil(bullet.distanceToTarget / bullet.speed).toInt)
    } else Math.max(1, source.expectedProjectileFrames(target))
    val volley = if (bulletType == BulletType.Yamato_Gun) {
      DamageVolley(260, 1, target.armorHealth, target.armorShield, Damage.Explosive(target.unitClass.size))
    } else {
      // Each observed projectile is one hit, not the source's entire multi-hit volley.
      source.damageVolleyAgainst(target).copy(hits = 1, hitChance = if (bullet.targetPixel.isDefined) 1.0 else source.hitChanceAgainst(target))
    }
    target.addDamage(source, inFrames, committed = true, automaticallyGuaranteed = false, volley = Some(volley), bulletId = Some(bullet.id))
  }

  @inline def countDamage(attacker: UnitInfo): Unit = {
    // Count damage that the unit is likely about to deal
    // Goal is to count whenever possible but being careful to not double-count damage that has already been dealt
    // Better to undercount damage than overcount it
    // A cooldown rise records the target at attack start, so retargeting cannot move this shot.
    // Once a projectile is visible, use that observation instead of counting the animation too.
    val attackFrameEnd = attacker.lastFrameStartingAttack + attacker.unitClass.stopFrames
    if (With.frame < attackFrameEnd && ! sourcesWithBullets.contains(attacker)) {
      attacker.lastAttackTarget.filter(attacker.isEnemyOf).foreach(target => {
        if (attacker.expectedProjectileFrames(target) > 0) {
          // Add frames for unit types with long projectile travel times.
          // The duration varies based on projectile travel time.
          val damageFrame = Math.max(1, attacker.lastFrameStartingAttack - With.frame + attacker.expectedProjectileFrames(target))
          target.addDamage(attacker, damageFrame, committed = true, automaticallyGuaranteed = false)
        }
      })
    }
    attacker.orderTarget.filter(attacker.isEnemyOf).filter(attacker.inRangeToAttack).foreach(_.addFutureAttack(attacker))
  }
}
