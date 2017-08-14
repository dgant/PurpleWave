package Macro.Decisions

class DesireScale(
  val min: Double = 0.0,
  val max: Double = Double.PositiveInfinity) {
  
  def scale(desired: Double): Double = {
    if (desired <= 0.0) {
      0.0
    }
    else {
      Math.max(min, Math.min(max, desired))
    }
  }
}
