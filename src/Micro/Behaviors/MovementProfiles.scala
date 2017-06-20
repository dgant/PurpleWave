package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = new MovementProfile(
    preferDestination     = 1.00,
    preferOrigin          = 0.30,
    preferMobility        = 0.30,
    preferThreatDistance  = 0.20,
    preferTarget          = 0.00,
    preferTargetValue     = 0.50,
    avoidTraffic          = 0.30,
    avoidDamage           = 0.15)
  
  def safelyAttackTarget = new MovementProfile(
    preferTarget          = 2.00,
    preferMobility        = default.preferMobility,
    avoidTraffic          = default.avoidTraffic,
    avoidDamage           = 1.00
  )
  
  def hoverOutsideRange = new MovementProfile(
    preferDestination = default.preferDestination,
    preferOrigin      = default.preferOrigin,
    preferMobility    = default.preferMobility,
    avoidTraffic      = default.avoidTraffic,
    avoidDamage       = 5.00
  )
  
  def flee = new MovementProfile(
    preferOrigin          = 1.00,
    preferThreatDistance  = 0.50,
    avoidDamage           = 1.00
  )
}
