package Planning.ResourceLocks

import Planning.Prioritized
import ProxyBwapi.Techs.Tech

class LockCurrencyForTech(prioritized: Prioritized, tech: Tech) extends LockCurrency(prioritized) {
  minerals = tech.mineralPrice
  gas = tech.gasPrice
}
