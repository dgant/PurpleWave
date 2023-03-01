package Mathematics.Functions

import Mathematics.Maff
import Mathematics.Points.Pixel

import scala.annotation.tailrec

trait Angles {
  val halfPI  : Double = Math.PI / 2
  val twoPI   : Double = 2 * Math.PI
  val inv2PI  : Double = 1.0 / twoPI
  val sqrt2   : Double = Math.sqrt(2)

  @inline @tailrec final def normalize0To2Pi(angleRadians: Double): Double = {
    if      (angleRadians < 0) normalize0To2Pi(angleRadians + twoPI)
    else if (angleRadians > twoPI) normalize0To2Pi(angleRadians - twoPI)
    else    angleRadians
  }

  @inline final def normalizePiToPi(angleRadians: Double): Double = {
    if      (angleRadians < -Math.PI) normalize0To2Pi(angleRadians + twoPI)
    else if (angleRadians > Math.PI) normalize0To2Pi(angleRadians - twoPI)
    else    angleRadians
  }

  @inline final def radiansTo(from: Double, to: Double): Double = {
    val distance = normalize0To2Pi(to - from)
    if (distance > Math.PI) distance - twoPI else distance
  }

  @inline final def slowAtan2(y: Double, x: Double): Double = normalize0To2Pi(Math.atan2(y, x))

  @inline final def isTowards(from: Pixel, to: Pixel, direction: Double): Boolean = {
    Math.abs(Maff.radiansTo(from.radiansTo(to), direction)) < Maff.halfPI
  }
}
