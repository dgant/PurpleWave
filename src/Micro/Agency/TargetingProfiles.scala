package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  1.0,
    preferDetectors   =  5.0,
    preferFocusFire   =  1.0,
    avoidPain         =  2.0,
    avoidDelay        =  0.15,
    avoidInterceptors =  2.0)
}
