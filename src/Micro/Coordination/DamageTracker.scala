package Micro.Coordination

import Lifecycle.With
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.BulletType

final class DamageTracker {

  // A lot of the ideas here are borrowed from Stardust's damage tracking

  // Bullets which travel in the air before doing damage,
  // and are mostly sure to hit (thereby excluding Valkyries and Lurkers)
  lazy val hangtime = Set(
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

  def update(): Unit = {
    With.units.all.foreach(_.clearDamage())
    With.bullets.all.foreach(countBullet)
    With.units.ours.foreach(countDamage)
    With.units.enemy.foreach(countDamage)
  }

  @inline def countBullet(bullet: BulletInfo): Unit = {
    if ( ! bullet.sourceUnit.exists(source => bullet.targetUnit.exists(_.isEnemyOf(source)))) return
    val source      = bullet.sourceUnit.get
    val target      = bullet.targetUnit.get
    val bulletType  = bullet.bulletType
    if (bulletType == BulletType.Yamato_Gun) {
      // TODO: Yamato damage
    } else if (bulletType == BulletType.Consume) {
      // TODO: Assume it's dead
    } else if (bulletType == BulletType.Queen_Spell_Carrier) {
      // Parasite or Spawn Broodling (Ensnare has its own type)
      if ( ! target.flying && ! target.unitClass.isRobotic) {
        // TODO: Assume it's dead from Spawn Broodling
      }
    } else if (hangtime.contains(bulletType)) {
      target.addDamage(source, if (bullet.moving) 0 else (bullet.pixel.pixelDistance(target.pixel) / bullet.speed).toInt, committed = true)
    }
  }

  @inline def countDamage(attacker: UnitInfo): Unit = {
    // Count damage that the unit is likely about to deal
    // Goal is to count whenever possible but being careful to not double-count damage that has already been dealt
    // Better to undercount damage than overcount it
    val target = attacker.orderTarget
    lazy val attackFrameStart = attacker.lastFrameStartingAttack
    lazy val attackFrameEnd = attackFrameStart + attacker.unitClass.stopFrames
    lazy val damageFrame = attackFrameEnd - With.frame + attacker.expectedProjectileFrames(target.get)
    if (target.exists(attacker.isEnemyOf)) {
      if (With.frame < attackFrameEnd) {
        // Add frames for unit types with long projectile travel times.
        // The duration varies based on projectile travel time.
        target.get.addDamage(attacker, damageFrame, committed = true)
      } else if (target.exists(attacker.inRangeToAttack)) {
        target.get.addFutureAttack(attacker)
      }
    }
  }

  @inline def projectedHitPoints(unit: UnitInfo): Int = {
    unit.hitPoints
  }

}
