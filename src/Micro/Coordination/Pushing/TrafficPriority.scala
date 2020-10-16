package Micro.Coordination.Pushing

import bwapi.Color

case class TrafficPriority(value: Int, color: Color, name: String) extends Ordered[TrafficPriority] {
  def compare(other: TrafficPriority): Int = value.compareTo(other.value)
}
