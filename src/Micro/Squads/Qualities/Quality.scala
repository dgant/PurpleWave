package Micro.Squads.Qualities

import Planning.UnitMatchers.UnitMatcher

trait Quality extends UnitMatcher {
  val counteredBy: Array[Quality] = Array.empty
  def counterScaling: Double = 1.0
}