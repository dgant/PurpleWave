package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = MovementProfile(
    preferDestination     = 1.0,
    preferOrigin          = 0.3,
    preferMobility        = 0.4,
    preferTarget          = 0.2,
    preferTargetValue     = 0.1,
    avoidTraffic          = 0.3,
    avoidExplosions       = 4.0,
    avoidDamage           = 2.0,
    avoidShovers          = 3.0)
  
  def safelyAttackTarget = new MovementProfile(default) {
    preferTarget          += 2.0
    avoidExplosions       += 1.0
    avoidDamage           += 1.0
  }
  
  def avoid = new MovementProfile(default) {
    avoidExplosions       += 2.0
    avoidDamage           += 2.0
  }
  
  def retreat = new MovementProfile(avoid) {
    preferDestination     = 0.00
    preferOrigin          = 1.00
  }
}
