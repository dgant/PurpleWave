package Tactic.Squads.Qualities

import Utilities.UnitFilters.UnitFilter

trait Quality extends UnitFilter {
  val counteredBy: Array[Quality] = Array.empty
  def counterScaling: Double = 1.0
}