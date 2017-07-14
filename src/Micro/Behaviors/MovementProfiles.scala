package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = MovementProfile(
    preferMobility        = 0.7,
    preferTarget          = 0.1,
    preferTargetValue     = 0.1,
    avoidTraffic          = 0.5,
    avoidExplosions       = 8.0,
    avoidDetection        = 8.0,
    avoidDamage           = 3.0,
    avoidShovers          = 3.0)
  
  def smash = new MovementProfile(default) {
    preferOrigin          = 0.0
    preferMobility        = 0.0
    avoidTraffic          = 0.0
    avoidDamage           = 0.0
  }
  
  def safelyAttackTarget = new MovementProfile(default) {
    preferTarget          += 0.5
    avoidExplosions       += 1.0
    avoidDamage           += 1.0
  }
  
  def avoid = new MovementProfile(default) {
    avoidExplosions       += 2.0
    avoidDamage           += 2.0
  }
}
