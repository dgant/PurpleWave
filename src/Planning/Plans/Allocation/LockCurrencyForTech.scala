package Planning.Plans.Allocation

import ProxyBwapi.Techs.Tech

class LockCurrencyForTech(tech: Tech)
  extends LockCurrency {
    minerals = tech.mineralPrice
    gas = tech.gasPrice
}
