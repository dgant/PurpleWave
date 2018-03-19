package Micro.Agency

import Micro.Heuristics.Targeting.TargetingProfile

object TargetingProfiles {
  
  def default = new TargetingProfile(
    preferVpfEnemy    =  0.5,
    preferVpfOurs     =  1.0,
    preferDetectors   =  12.0,
    preferFocusFire   =  0.2,
    //avoidPain         =  2.0,
    avoidPain         =  0.0,
    avoidDelay        =  0.18,
    avoidInterceptors =  2.0)
}
