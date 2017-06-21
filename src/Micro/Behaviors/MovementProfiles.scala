package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.30,
    preferTarget          = 0.00,
    preferTargetValue     = 0.50,
    avoidTraffic          = 0.30,
    avoidExplosions       = 2.00,
    avoidDamage           = 0.50)
  
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
    preferTarget          = 2.00,
    preferMobility        = default.preferMobility,
    avoidTraffic          = default.avoidTraffic,
    avoidExplosions       = 3.00,
    avoidDamage           = 1.00
  )
  
  def rout = new MovementProfile(
    preferOrigin      = 1.00,
    preferMobility    = default.preferMobility,
    avoidTraffic      = default.avoidTraffic,
    avoidExplosions   = 5.00,
    avoidDamage       = 5.00
  )
  
  def hoverOutsideRange = new MovementProfile(
    preferDestination = default.preferDestination,
    preferOrigin      = default.preferOrigin,
    preferMobility    = default.preferMobility,
    avoidTraffic      = default.avoidTraffic,
    avoidDamage       = rout.avoidDamage
  )
  
  
}
