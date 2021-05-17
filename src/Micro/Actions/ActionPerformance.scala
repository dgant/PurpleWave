package Micro.Actions

class ActionPerformance {
  var invocations: Int = 0
  var durationNanos: Long = 0
  def totalMs: Double = durationNanos / 1e6
  def meanMs: Double = totalMs / invocations.toDouble
}
