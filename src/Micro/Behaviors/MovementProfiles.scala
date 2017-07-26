package Micro.Behaviors

import Micro.Heuristics.Movement.MovementProfile

object MovementProfiles {
  
  def default = MovementProfile(
    preferVpfDealing      = 1.0,
    preferMobility        = 1.0,
    avoidVpfReceiving     = 1.0,
    avoidTraffic          = 1.0,
    avoidDamage           = 1.0,
    avoidShovers          = 1.0)
  
  def avoid = new MovementProfile(default) {
    preferVpfDealing      = 0.0
  }
  
  def smash = new MovementProfile(default) {
    avoidVpfReceiving     = 0.0
    avoidTraffic          = 0.0
    avoidDamage           = 0.0
  }
}
