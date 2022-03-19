package Macro.Scheduling

import Performance.Cache

class MacroCounts {
  def oursExtant = _oursExtant()
  def oursComplete = _oursComplete()
  private val _oursExtant = new Cache(() => MacroCounter.countOursExtant)
  private val _oursComplete = new Cache(() => MacroCounter.countOursComplete)
}
