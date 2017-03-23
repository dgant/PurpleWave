package Planning.Plans.Allocation

import ProxyBwapi.Techs.Tech

class LockCurrencyForTech(tech: Tech) extends LockCurrency {
    
  description.set(tech.toString)
  
  minerals = tech.mineralPrice
  gas = tech.gasPrice
}
