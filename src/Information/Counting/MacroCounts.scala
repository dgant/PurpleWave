package Information.Counting

import Performance.Cache
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

class MacroCounts {
  def oursExtant: CountMap[UnitClass] = _oursExtant()
  def oursComplete: CountMap[UnitClass] = _oursComplete()
  private val _oursExtant = new Cache(() => MacroCounter.countOursExtant)
  private val _oursComplete = new Cache(() => MacroCounter.countOursComplete)
}
