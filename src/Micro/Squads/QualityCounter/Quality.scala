package Micro.Squads.QualityCounter

import Planning.UnitMatchers.UnitMatcher

trait Quality extends UnitMatcher {
  val counteredBy: Array[Quality] = Array.empty
  @inline def counterScaling: Double = 1.0
}