package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.40,
    preferTarget          = 0.00,
    preferTargetValue     = 0.50,
    avoidTraffic          = 0.30,
    avoidExplosions       = 4.00,
    avoidDamage           = 2.00)
  
  def engage = new MovementProfile(
    preferDestination = default.preferDestination,
    preferOrigin      = default.preferOrigin,
    preferMobility    = default.preferMobility,
    preferTarget      = 1.00,
    preferTargetValue = 1.00,
    avoidTraffic      = default.avoidTraffic,
    avoidExplosions   = default.avoidExplosions,
    avoidDamage       = default.avoidDamage)
  
  def safelyAttackTarget = new MovementProfile(
    preferTarget      = 2.00,
    preferMobility    = default.preferMobility,
    avoidTraffic      = default.avoidTraffic,
    avoidExplosions   = 1.0 + default.avoidExplosions,
    avoidDamage       = 1.0 + default.avoidDamage
  )
  
  def rout = new MovementProfile(
    preferOrigin      = 1.00,
    preferMobility    = default.preferMobility,
    avoidTraffic      = 0.5 + default.avoidTraffic,
    avoidExplosions   = 2.0 + default.avoidExplosions,
    avoidDamage       = 2.0 + default.avoidDamage
  )
  
  def hoverOutsideRange = new MovementProfile(
    preferDestination = default.preferDestination,
    preferOrigin      = default.preferOrigin,
    preferMobility    = default.preferMobility,
    avoidTraffic      = default.avoidTraffic,
    avoidExplosions   = rout.avoidExplosions,
    avoidDamage       = rout.avoidDamage
  )
  
}
