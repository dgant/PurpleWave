package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy  =  0.4,
    preferVpfOurs   =  0.8,
    preferDetectors =  8.0,
    avoidDelay      =  1.0,
    avoidPain       =  0.3
  )
}
