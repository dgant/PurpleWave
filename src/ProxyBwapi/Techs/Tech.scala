package ProxyBwapi.Techs

import bwapi.TechType

case class Tech(base:TechType) {
  val energyCost    = base.energyCost
  val gasPrice      = base.gasPrice
  val mineralPrice  = base.mineralPrice
  val researchTime  = base.researchTime
  
  /*
    Not implemented:
    
    getOrder
    getRace
    getWeapon
    requiredUnit
    targetsPosition
    targetsUnit
    whatResearches
    whatUses
   */
}
